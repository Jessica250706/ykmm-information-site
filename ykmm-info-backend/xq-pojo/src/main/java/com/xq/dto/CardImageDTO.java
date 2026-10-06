package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面图片参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardImageDTO implements Serializable {
    /** 图片类型：1-R 2-SR 3-SSR 4-UR竖卡 5-UR横卡 */
    private Integer imageType;
    /** 图片地址 */
    private String url;
    /** 排序 */
    private Integer sort;
}
