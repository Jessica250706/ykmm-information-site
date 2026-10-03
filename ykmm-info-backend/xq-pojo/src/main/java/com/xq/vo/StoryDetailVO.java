package com.xq.vo;

import lombok.Data;

import java.util.List;

/**
 * 剧情详情（含对话）
 */
@Data
public class StoryDetailVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 所属分类ID
     */
    private Long categoryId;
    /**
     * 分类名
     */
    private String categoryName;
    /**
     * 分类类型
     */
    private Integer categoryType;
    /**
     * 分类类型标签
     */
    private String categoryTypeLabel;
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
    /**
     * 创建时间
     */
    private String createdAt;
    /**
     * 更新时间
     */
    private String updatedAt;
    /**
     * 对话版本列表
     */
    private List<DialogueVersionVO> versions;
}
