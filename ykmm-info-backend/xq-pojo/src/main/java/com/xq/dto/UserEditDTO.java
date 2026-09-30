package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserEditDTO implements Serializable {

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
     * 1正常 0禁用
     */
    private Integer status;
}
