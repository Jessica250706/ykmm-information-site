package com.xq.dto;

import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {

  /** 邮箱 */
  private String email;

  /** 密码 */
  private String password;

  /** 昵称 */
  private String nickname;
}
