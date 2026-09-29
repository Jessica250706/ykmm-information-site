package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面图片表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 卡面ID */
    private Long cardId;

    /** 1-R 2-SR 3-SSR 4-UR竖卡 5-UR横卡 */
    private Integer imageType;

    /** 图片地址 */
    private String url;

    /** 排序 */
    private Integer sort;
}
