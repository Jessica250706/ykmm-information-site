package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表情包分组VO
 */
@Data
public class StickerGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分组名称
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

    /**
     * 分组下表情包数量
     */
    private Long stickerCount;
}
