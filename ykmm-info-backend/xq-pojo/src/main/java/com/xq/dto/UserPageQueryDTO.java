package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageQueryDTO extends PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键字：邮箱 / 昵称 / uid
     */
    private String keyword;

    /**
     * 状态：1正常 0禁用，null不限
     */
    private Integer status;
}