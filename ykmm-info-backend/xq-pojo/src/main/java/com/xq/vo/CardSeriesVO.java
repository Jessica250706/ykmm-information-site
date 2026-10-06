package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面系列返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSeriesVO implements Serializable {
    /** 主键 */
    private Long id;
    /** 系列名 */
    private String name;
    /** 系列描述 */
    private String description;
    /** 封面图地址 */
    private String coverImage;
    /** 关联卡面数量 */
    private Integer cardCount;
    /** 创建时间 */
    private String createdAt;
    /** 更新时间 */
    private String updatedAt;
}
