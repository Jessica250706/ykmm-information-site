package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册成功响应（包含 token 和用户信息）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    /**
     * JWT 令牌
     */
    private String token;

    /**
     * 当前登录用户信息
     */
    private UserInfo user;
}
