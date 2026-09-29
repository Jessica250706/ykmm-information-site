package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserStatusDTO implements Serializable {

    /**
     * 1正常 0禁用
     */
    private Integer status;
}
