package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表情包资源表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sticker implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 标签，如：国王布丁表情包 */
    private String label;

    /** 图片地址 */
    private String imageUrl;

    /** 对应emoji，如 🍮 */
    private String emoji;

    /** 1自定义图片 2 emoji */
    private Integer stickerType;

    /** 创建时间 */
    private LocalDateTime createdAt;
}