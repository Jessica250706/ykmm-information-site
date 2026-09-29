package com.xq.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

  private String token;

  private UserInfo user;
}
