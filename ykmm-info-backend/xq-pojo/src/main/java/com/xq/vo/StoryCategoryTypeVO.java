package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 剧情分类类型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryCategoryTypeVO {
    /**
     * 主键
     */
    private Integer id;
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
    /**
     * 创建时间
     */
    private String createdAt;
    /**
     * 更新时间
     */
    private String updatedAt;
}
