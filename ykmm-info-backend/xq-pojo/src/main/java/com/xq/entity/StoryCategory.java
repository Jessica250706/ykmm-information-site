package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 剧情分类树
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 父分类ID */
    private Long parentId;

    /** 分类名 */
    private String name;

    /** 1主线 2彩虹城 3特别篇 4活动篇 5戏剧篇 */
    private Integer categoryType;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
