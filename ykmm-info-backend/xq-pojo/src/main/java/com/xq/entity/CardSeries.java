package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡面系列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSeries implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 系列名 */
    private String name;

    /** 描述 */
    private String description;

    /** 封面图 */
    private String coverImage;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}