package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色分页查询条件
 */
@Data
public class RolePageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 关键字：角色名
     */
    private String keyword;

    /**
     * 对应人物ID
     */
    private Long personId;

    /**
     * 剧情分类根节点ID
     */
    private Long storyCategoryId;
}
