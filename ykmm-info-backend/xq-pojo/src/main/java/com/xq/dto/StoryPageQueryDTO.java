package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧情分页查询条件
 */
@Data
public class StoryPageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 关键字：标题模糊匹配
     */
    private String keyword;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 分类类型：1主线 2彩虹城 3特别篇 4活动篇 5戏剧篇
     */
    private Integer categoryType;

    /**
     * 审核状态：1已发布 2待审核 3已拒绝
     */
    private Integer status;
}
