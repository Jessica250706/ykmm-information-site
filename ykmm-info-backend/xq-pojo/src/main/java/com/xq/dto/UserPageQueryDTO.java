package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 关键字：邮箱 / 昵称 / uid
     */
    private String keyword;

    /**
     * 状态：1正常 0禁用，null不限
     */
    private Integer status;
}
