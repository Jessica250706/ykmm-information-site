package com.xq.dto;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {

  /** 邮箱 */
  private String email;

  /** 密码 */
  private String password;
}
