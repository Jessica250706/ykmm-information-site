package com.xq.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色新增/编辑
 */
@Data
public class RoleDTO implements Serializable {

    /**
     * 对应人物ID，可为空
     */
    private Long personId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色简介
     */
    private String intro;

    /**
     * 所属剧情分类根节点ID列表
     */
    private List<Long> storyCategoryIds;
}
