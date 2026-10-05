package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 剧情分类返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryCategoryVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 父分类名
     */
    private String parentName;

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
     * 分类类型标签
     */
    private String categoryTypeLabel;

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

    /**
     * 子分类
     */
    private List<StoryCategoryVO> children;
}
