package com.xq.interceptor;

import com.xq.annotation.RequireRole;
import com.xq.constant.JwtClaimsConstant;
import com.xq.context.BaseContext;
import com.xq.enums.UserRoleEnum;
import com.xq.exception.QuizRequiredException;
import com.xq.service.UserService;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.properties.JwtProperties;
import com.xq.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    /** 公开路径前缀：可选认证，游客与登录用户共用 */
    private static final List<String> PUBLIC_PREFIXES = List.of("/pub");

    /** 完全放行的路径：不解析 token */
    private static final List<String> SKIP_PREFIXES = List.of(
            "/auth/login", "/auth/register"
    );
    /**
     * 答题门槛豁免路径：
     * - /user/quiz/**  ：答题接口本身，未通过的用户必须能访问
     * - /user/user/**  ：个人信息（改密码、换绑邮箱、查看 status），
     *                    未通过答题也要能改资料
     */
    private static final List<String> QUIZ_EXEMPT_PREFIXES = List.of(
            "/user/quiz",
            "/user/user"
    );

    private final JwtProperties jwtProperties;
    private final UserService userService;

    /** Spring 的工具类：自动剥离 contextPath 与 servletPath */
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 非 Controller 方法直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 2. CORS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 3. 取真实的业务路径，例如 /pub/cards、/auth/login、/user/profile
        String path = urlPathHelper.getLookupPathForRequest(request);

        // 4. 完全跳过的路径
        if (matchAny(path, SKIP_PREFIXES)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        boolean hasToken = authHeader != null && authHeader.startsWith("Bearer ");

        // 5. 公开路径：可选认证
        if (matchAny(path, PUBLIC_PREFIXES)) {
            if (hasToken) {
                tryParseAndSetContext(authHeader.substring(7), false);
            }
            return true;
        }

        // 6. 非公开路径：必须登录
        if (!hasToken) {
            throw new UnauthorizedException("未登录");
        }
        tryParseAndSetContext(authHeader.substring(7), true);

        // 7. 权限校验
        checkPermission(path, handlerMethod, BaseContext.getCurrentRole());

        // 答题门槛校验
        checkQuizRequirement(path);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.remove();
    }

    private void tryParseAndSetContext(String token, boolean throwOnError) {
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);

            Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
            Object roleObj = claims.get(JwtClaimsConstant.ROLE);

            if (userIdObj == null || roleObj == null) {
                throw new IllegalStateException("token 缺少必要 claim");
            }

            Long userId = Long.valueOf(userIdObj.toString());
            Integer role = Integer.valueOf(roleObj.toString());

            BaseContext.setCurrentId(userId);
            BaseContext.setCurrentRole(role);
            log.debug("token 解析成功：userId={}, role={}", userId, role);
        } catch (Exception e) {
            if (throwOnError) {
                log.error("token 解析失败：{}", e.getMessage());
                throw new UnauthorizedException("登录已失效，请重新登录");
            }
            // 公开路径：token 无效当游客处理
            log.warn("公开接口 token 无效，按游客处理：{}", e.getMessage());
            BaseContext.remove();
        }
    }

    private void checkPermission(String path, HandlerMethod handlerMethod, Integer role) {
        // 1. 路径硬约束：/admin/** 必须是管理员（优先级最高，任何 @RequireRole 都绕不过去）
        if (isAdminPath(path) && !UserRoleEnum.ADMIN.getValue().equals(role)) {
            throw new ForbiddenException("无权限访问");
        }

        // 2. 注解约束：方法或类上的 @RequireRole
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (annotation != null) {
            for (int allowed : annotation.value()) {
                if (role != null && allowed == role) {
                    return;
                }
            }
            throw new ForbiddenException("无权限访问");
        }
    }

    /**
     * 判断是否为管理端路径。
     * 用 "/admin" 或 "/admin/xxx" 精确匹配，避免误伤 /administrator 等。
     */
    private boolean isAdminPath(String path) {
        return "/admin".equals(path) || path.startsWith("/admin/");
    }

    /** 前缀匹配：/pub 能匹配 /pub 和 /pub/xxx，不会误伤 /public */
    private boolean matchAny(String path, List<String> prefixes) {
        for (String p : prefixes) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 答题门槛校验：
     * <p>
     * 规则：
     * <ul>
     *   <li>仅对 /user/** 路径生效（/admin 由 checkPermission 已强制管理员，无需再判断）</li>
     *   <li>豁免路径（答题接口、个人信息）跳过</li>
     *   <li>管理员豁免</li>
     *   <li>其他用户：必须通过答题，否则 403</li>
     * </ul>
     *
     * @param path 请求路径
     */
    private void checkQuizRequirement(String path) {
        // 非 /user 路径不管
        if (!("/user".equals(path) || path.startsWith("/user/"))) {
            return;
        }

        // 豁免路径跳过
        if (matchAny(path, QUIZ_EXEMPT_PREFIXES)) {
            return;
        }

        // 管理员豁免
        Integer role = BaseContext.getCurrentRole();
        if (UserRoleEnum.ADMIN.getValue().equals(role)) {
            return;
        }

        // 用户必须已通过答题
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            // 理论上不会到这，前置已校验过登录
            throw new UnauthorizedException("未登录");
        }
        if (!userService.canUseUserFeature(userId)) {
            log.warn("用户未通过答题，拒绝访问：userId={}, path={}", userId, path);
            throw new QuizRequiredException();
        }
    }
}
