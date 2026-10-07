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

    /** 关键字：卡面名称模糊匹配 */
    private String keyword;

    /** 所属系列ID */
    private Long seriesId;

    /** 最高等级：1-SSR 2-UR */
    private Integer maxRarity;

    /** 属性 */
    private Integer attribute;

    /** 状态：1已发布 2待审核 3已拒绝 */
    private Integer status;

    /** 关联人物ID列表（多选，任选其一即匹配） */
    private List<Long> personIds;
}
