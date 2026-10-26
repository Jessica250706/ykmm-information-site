package com.xq.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 卡面来源轻量 VO
 * <p>
 * 用于"对话版本来源提示"等只需要「卡面名 + 系列名」的场景，
 * 一次 join 查询即可拿全，避免逐级调用 card / card_series。
 */
@Data
public class CardSourceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 卡面ID */
    private Long id;

    /** 卡面名称 */
    private String name;

    /** 所属系列ID（保留，便于调用方判断是否有系列） */
    private Long seriesId;

    /** 所属系列名（LEFT JOIN，无系列时为 null） */
    private String seriesName;

    /**
     * 状态
     */
    private Integer status;
}
