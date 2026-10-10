package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表情包VO
 */
@Data
public class StickerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 标签
     */
    private String label;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 对应emoji
     */
    private String emoji;

    /**
     * 1自定义图片 2 emoji
     */
    private Integer stickerType;

    /**
     * 表情包类型描述
     */
    private String stickerTypeDesc;

    /**
     * 创建者用户ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
