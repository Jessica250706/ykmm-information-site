package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面类别返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCategoryVO implements Serializable {

    /** 主键 */
    private Integer id;

    /** 类别名 */
    private String name;

    /** 排序 */
    private Integer sort;

    /** 关联的卡面数量（仅分页查询返回，其它接口为 null） */
    private Integer cardCount;
}
