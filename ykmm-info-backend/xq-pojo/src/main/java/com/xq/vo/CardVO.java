package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 卡面返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardVO implements Serializable {
    /** 主键 */
    private Long id;
    /** 卡面名称 */
    private String name;
    /** 系列ID */
    private Long seriesId;
    /** 系列名 */
    private String seriesName;
    /** 关联 card_category.id */
    private Integer category;
    /** 最高等级 */
    private Integer maxRarity;
    /** 最高等级标签 */
    private String maxRarityLabel;
    /** 首次入池时间 */
    private LocalDate firstPoolTime;
    /** 属性 */
    private Integer attribute;
    /** 属性标签 */
    private String attributeLabel;
    /** 魅力技能描述 */
    private String skillDesc;
    /** 附属剧情类型 */
    private Integer attachedStoryType;
    /** 附属剧情类型标签 */
    private String attachedStoryTypeLabel;
    /** 服装类型 */
    private Integer costumeType;
    /** 服装类型标签 */
    private String costumeTypeLabel;
    /** 偶像小人ID */
    private Long chibiId;
    /** 造型ID */
    private Long costumeId;
    /** 状态 */
    private Integer status;
    /** 状态标签 */
    private String statusLabel;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 卡面图片列表 */
    private List<CardImageVO> images;
    /** 关联人物列表 */
    private List<CardPersonVO> persons;
}
