package com.xq.vo;

import com.xq.entity.SysUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息（响应视图对象）
 */
@Data
@Builder
public class UserInfo {

    /**
     * 主键
     */
    private Long id;

    /**
     * 随机唯一标识
     */
    private String uid;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 最后上线时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 1正常 0禁用
     */
    private Integer status;

    /**
     * 角色：1-管理员 2-普通用户
     */
    private Integer role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从 SysUser 实体构建 UserInfo（用于脱敏后返回给前端）
     */
    public static UserInfo from(SysUser user) {
        return UserInfo.builder()
                .id(user.getId())
                .uid(user.getUid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .lastLoginTime(LocalDateTime.now())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
