package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧情分类分页查询条件
 */
@Data
public class StoryCategoryPageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 分类类型
     */
    Integer categoryType;

    /**
     * 父分类ID：有值时只分页该节点的直接子节点
     */
    private Long parentId;
}
