package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面图片返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardImageVO implements Serializable {
    /** 主键 */
    private Long id;
    /** 图片类型 */
    private Integer imageType;
    /** 图片类型标签 */
    private String imageTypeLabel;
    /** 图片地址 */
    private String url;
    /** 排序 */
    private Integer sort;
}
