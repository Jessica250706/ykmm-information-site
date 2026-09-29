package com.xq.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 密码哈希（BCrypt）
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 最后登录IP
     */
    private String ipAddress;

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
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
