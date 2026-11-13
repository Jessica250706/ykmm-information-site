package com.xq.dto;

import lombok.*;

import java.util.List;

/**
 * 卡面分页查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CardPageQueryDTO extends PageQueryDTO {

    /**
     * 关键字：卡面名称模糊匹配
     */
    private String keyword;

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
     * 属性
     */
    private Integer attribute;

    /**
     * 附属剧情类型：0无 1RC 2RTV 3Rabbiter
     */
    private Integer attachedStoryType;

    /**
     * 服装类型：0无 1偶像小人 2 3D造型
     */
    private Integer costumeType;

    /**
     * 状态：1已发布 2待审核 3已拒绝
     */
    private Integer status;

    /**
     * 关联人物ID列表（多选，任选其一即匹配）
     */
    private List<Long> personIds;
}
