package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 卡面类别分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CardCategoryPageQueryDTO extends PageQueryDTO {

    /** 类别名模糊搜索 */
    private String keyword;
}
