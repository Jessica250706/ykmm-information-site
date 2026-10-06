package com.xq.dto;

import lombok.*;

/**
 * 卡面系列分页查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CardSeriesPageQueryDTO extends PageQueryDTO {
    /** 关键字：系列名模糊匹配 */
    private String keyword;
}
