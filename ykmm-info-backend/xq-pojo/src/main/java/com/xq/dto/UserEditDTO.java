package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserEditDTO implements Serializable {

    private String email;
    private String nickname;
    private String avatar;

    /**
     * 1正常 0禁用
     */
    private Integer status;
}
