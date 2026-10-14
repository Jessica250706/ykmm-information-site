package com.xq.interceptor;

import com.xq.annotation.RequireRole;
import com.xq.constant.JwtClaimsConstant;
import com.xq.constant.UserRoleConstant;
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

import java.util.List;

/**
 * JWT 令牌校验拦截器
 * <p>
 * 支持两种模式：
 * 1. 强制认证：/user/**、/admin/** 等，必须有有效 token
 * 2. 可选认证：/pub/** 等公开接口，有 token 则解析身份，无 token 则当作游客
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    /**
     * 公开路径前缀：可选认证
     */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/pub/"
    );

    /**
     * 完全放行的路径：不需要解析 token
     */
    private static final List<String> SKIP_PREFIXES = List.of(
            "/auth/login", "/auth/register"
    );

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 非动态方法直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 2. CORS 预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // 3. 完全跳过的路径（登录、注册）
        for (String p : SKIP_PREFIXES) {
            if (uri.startsWith(p)) return true;
        }

        String authHeader = request.getHeader("Authorization");
        boolean hasToken = authHeader != null && authHeader.startsWith("Bearer ");

        // 4. 公开路径：可选认证
        boolean isPublicPath = PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith);
        if (isPublicPath) {
            if (hasToken) {
                tryParseAndSetContext(authHeader.substring(7), false);
            }
            // 无论是否有 token，公开路径都放行
            return true;
        }

        // 5. 非公开路径：必须登录
        if (!hasToken) {
            throw new UnauthorizedException("未登录");
        }
        tryParseAndSetContext(authHeader.substring(7), true);

        // 6. 权限校验
        checkPermission(request, handlerMethod, BaseContext.getCurrentRole());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.remove();
    }

    /**
     * 解析 token 并写入 BaseContext
     *
     * @param token        token 字符串（已去掉 "Bearer "）
     * @param throwOnError true 时解析失败抛 401；false 时吞掉异常（游客身份）
     */
    private void tryParseAndSetContext(String token, boolean throwOnError) {
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            Integer role = Integer.valueOf(claims.get(JwtClaimsConstant.ROLE).toString());
            BaseContext.setCurrentId(userId);
            BaseContext.setCurrentRole(role);
            log.debug("token 解析成功：userId={}, role={}", userId, role);
        } catch (Exception e) {
            if (throwOnError) {
                log.error("token 解析失败：{}", e.getMessage());
                throw new UnauthorizedException("登录已失效，请重新登录");
            }
            // 公开路径：token 无效当游客处理，不报错
            log.warn("公开接口 token 无效，按游客处理：{}", e.getMessage());
        }
    }

    /**
     * 权限校验
     */
    private void checkPermission(HttpServletRequest request, HandlerMethod handlerMethod, Integer role) {
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (annotation != null) {
            for (int allowed : annotation.value()) {
                if (allowed == role) return;
            }
            throw new ForbiddenException("无权限访问");
        }

        // 路径兜底
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/") && !UserRoleEnum.ADMIN.getValue().equals(role)) {
            throw new ForbiddenException("无权限访问");
        }
    }
}
