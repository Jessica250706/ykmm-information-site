package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 剧情表（话）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Story implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属分类节点 */
    private Long categoryId;

    /** 话标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 1已发布 2待审核 3已拒绝 */
    private Integer status;

    /** 创建者用户ID */
    private Long creatorId;

    /** 审核人用户ID */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 审核备注 */
    private String reviewRemark;
}
