package com.xq.interceptor;

import com.xq.annotation.RequireRole;
import com.xq.constant.JwtClaimsConstant;
import com.xq.context.BaseContext;
import com.xq.properties.JwtProperties;
import com.xq.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

/**
 * jwt令牌校验的拦截器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("JwtTokenInterceptor.preHandle 被调用，URI={}", request.getRequestURI());

        // 判断当前拦截到的是 Controller 的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getTokenName());
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // 2、校验令牌
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
        } catch (Exception e) {
            log.error("token 解析失败：{}", e.getMessage());
            response.setStatus(401);
            return false;
        }

        // 3. 写入上下文
        Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
        Integer role = Integer.valueOf(claims.get(JwtClaimsConstant.ROLE).toString());
        log.info("当前员工id：{}，角色是：{}", userId, role);
        BaseContext.setCurrentId(userId);
        BaseContext.setCurrentRole(role);

        // 4. 权限校验
        return checkPermission(request, role, response);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }

    // TODO: role 从 字符串 修改为 Integer
    private boolean checkPermission(HttpServletRequest request, Integer role, HttpServletResponse response) {
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handlerMethod == null) {
            return true;
        }

        // 方法上的注解优先，其次类上的
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (annotation == null) {
            return true;   // 没注解 = 登录即可访问
        }

        for (int allowed : annotation.value()) {
            if (allowed == role) {
                return true;
            }
        }

        response.setStatus(403);
        return false;
    }
}
