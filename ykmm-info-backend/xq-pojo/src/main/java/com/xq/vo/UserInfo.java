package com.xq.vo;

import com.xq.entity.SysUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserInfo {

  private Long id;

  private String uid;

  private String email;

  private String nickname;

  private String avatar;

  private Integer status;

  private LocalDateTime createdAt;

  public static UserInfo from(SysUser user) {
    return UserInfo.builder()
        .id(user.getId())
        .uid(user.getUid())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .avatar(user.getAvatar())
        .status(user.getStatus())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
