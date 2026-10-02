package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 剧情返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属分类节点
     */
    private Long categoryId;

    /**
     * 所属分类名
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
     * 审核状态
     */
    private Integer status;

    /**
     * 审核状态标签
     */
    private String statusLabel;

    /**
     * 创建者用户ID
     */
    private Long creatorId;

    /**
     * 审核人用户ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
