package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色简要信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
