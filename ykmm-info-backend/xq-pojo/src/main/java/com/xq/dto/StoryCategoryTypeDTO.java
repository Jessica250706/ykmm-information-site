package com.xq.dto;

import lombok.Data;

/**
 * 剧情分类类型编辑参数
 */
@Data
public class StoryCategoryTypeDTO {
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 标签颜色
     */
    private String color;
    /**
     * 排序
     */
    private Integer sort;
}