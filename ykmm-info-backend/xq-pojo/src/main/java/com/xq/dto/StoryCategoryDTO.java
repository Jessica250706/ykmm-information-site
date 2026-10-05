package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧情分类新增/编辑
 */
@Data
public class StoryCategoryDTO implements Serializable {

    /**
     * 父分类ID，0 表示根节点
     */
    private Long parentId;

    /**
     * 分类名
     */
    private String name;

    /**
     * 分类简介
     */
    private String description;

    /**
     * 1主线 2彩虹城 3特别篇 4活动篇 5戏剧篇
     */
    private Integer categoryType;

    /**
     * 排序
     */
    private Integer sort;
}
