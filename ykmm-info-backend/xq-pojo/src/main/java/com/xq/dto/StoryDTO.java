package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧情新增/编辑
 */
@Data
public class StoryDTO implements Serializable {

    /**
     * 所属分类节点
     */
    private Long categoryId;

    /**
     * 话标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
