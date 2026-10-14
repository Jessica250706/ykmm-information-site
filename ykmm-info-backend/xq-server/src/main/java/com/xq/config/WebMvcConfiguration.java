package com.xq.config;

import com.xq.interceptor.JwtTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证：登录、注册（游客可访问）
                        "/auth/login",
                        "/auth/register",
                        // Swagger / Knife4j
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        // 错误页
                        "/error");
    }
}
