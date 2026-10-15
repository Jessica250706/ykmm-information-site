package com.xq.interceptor;

import com.xq.annotation.RequireRole;
import com.xq.constant.JwtClaimsConstant;
import com.xq.context.BaseContext;
import com.xq.enums.UserRoleEnum;
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

    private final JwtProperties jwtProperties;

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
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (annotation != null) {
            for (int allowed : annotation.value()) {
                if (role != null && allowed == role) return;
            }
            throw new ForbiddenException("无权限访问");
        }

        // 路径兜底
        if (path.startsWith("/admin") && !UserRoleEnum.ADMIN.getValue().equals(role)) {
            throw new ForbiddenException("无权限访问");
        }
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
}
