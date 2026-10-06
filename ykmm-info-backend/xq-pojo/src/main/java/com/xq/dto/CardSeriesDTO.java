package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面系列新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSeriesDTO implements Serializable {
    /** 系列名 */
    private String name;
    /** 系列描述 */
    private String description;
    /** 封面图地址 */
    private String coverImage;
}
