package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表情包分组表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StickerGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分组名称，如：国王布丁、奇娜子
     */
    private String name;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建者用户ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
