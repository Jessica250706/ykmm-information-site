package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 卡面表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 卡面名称 */
    private String name;

    /** 所属系列 */
    private Long seriesId;

    /** 关联 card_category.id */
    private Integer category;

    /** 最高等级：1-SSR 2-UR */
    private Integer maxRarity;

    /** 首次入池时间 */
    private LocalDate firstPoolTime;

    /** 1-Shout 2-Beat 3-Melody */
    private Integer attribute;

    /** 魅力技能描述 */
    private String skillDesc;

    /** 0无 1RC 2RTV 3Rabbiter */
    private Integer attachedStoryType;

    /** 1偶像小人 2 3D造型 */
    private Integer costumeType;

    /** 小人ID */
    private Long chibiId;

    /** 造型ID */
    private Long costumeId;

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
