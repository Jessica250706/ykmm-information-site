package com.xq.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 剧情分类类型字典
 */
@Data
public class StoryCategoryType {

    /**
     * 主键，与枚举值对应
     */
    private Integer id;

    /**
     * 分类类型名称
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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
