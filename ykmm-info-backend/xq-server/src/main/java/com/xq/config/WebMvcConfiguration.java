package com.xq.config;

import com.xq.interceptor.JwtTokenAdminInterceptor;
import com.xq.interceptor.JwtTokenUserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

  private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

  private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {

    registry.addInterceptor(jwtTokenUserInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/auth/register",
            "/auth/login",
            "/error");

    registry.addInterceptor(jwtTokenAdminInterceptor)
        .addPathPatterns("/admin/**")
        .excludePathPatterns(
            "/admin/auth/login",
            "/error");
  }
}
