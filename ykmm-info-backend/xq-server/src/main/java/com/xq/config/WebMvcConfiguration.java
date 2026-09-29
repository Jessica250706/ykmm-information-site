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
                        "/auth/login",
                        "/auth/register",
                        "/common/dict/**",       // 字典类公开接口
                        "/doc.html", "/webjars/**", "/v3/api-docs/**",  // Knife4j
                        "/error");
    }
}
