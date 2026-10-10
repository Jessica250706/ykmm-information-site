package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 卡面新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO implements Serializable {
    /**
     * 卡面名称
     */
    private String name;
    /**
     * 所属系列ID
     */
    private Long seriesId;
    /**
     * 关联 card_category.id
     */
    private Integer category;
    /**
     * 最高等级：1-SSR 2-UR
     */
    private Integer maxRarity;
    /**
     * 首次入池时间
     */
    private LocalDate firstPoolTime;
    /**
     * 属性：1-Shout 2-Beat 3-Melody
     */
    private Integer attribute;
    /**
     * 魅力技能描述
     */
    private String skillDesc;
    /**
     * 附属剧情类型：0无 1RC 2RTV 3Rabbiter
     */
    private Integer attachedStoryType;
    /**
     * 服装类型：1偶像小人 2 3D造型
     */
    private Integer costumeType;
    /**
     * 偶像小人ID
     */
    private Long chibiId;
    /**
     * 造型ID
     */
    private Long costumeId;
    /**
     * 关联人物ID列表
     */
    private List<Long> personIds;
    /**
     * 卡面图片列表
     */
    private List<CardImageDTO> images;
}
