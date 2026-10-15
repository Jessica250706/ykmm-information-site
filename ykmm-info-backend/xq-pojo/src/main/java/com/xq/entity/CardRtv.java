package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡面RTV表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRtv implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 卡面ID
     */
    private Long cardId;

    /**
     * 第几话
     */
    private Integer episodeNo;

    /**
     * 标题
     */
    private String title;

    /**
     * 创建者用户ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}