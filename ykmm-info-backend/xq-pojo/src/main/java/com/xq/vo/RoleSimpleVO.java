package com.xq.vo;

import lombok.Data;

/**
 * 角色简要信息
 */
@Data
public class RoleSimpleVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 角色名
     */
    private String name;
    /**
     * 角色简介
     */
    private String intro;
    /**
     * 对应人物ID，null 表示无对应人物
     */
    private Long personId;
}
