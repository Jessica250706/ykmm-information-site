package com.xq.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录/注册成功响应（包含 token 和用户信息）
 */
@Data
@Builder
public class LoginResponse {

  /** JWT 令牌 */
  private String token;

  /** 当前登录用户信息 */
  private UserInfo user;
}
