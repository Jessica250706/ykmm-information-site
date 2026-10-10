package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 表情包分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StickerPageQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 标签关键字
     */
    private String label;

    /**
     * 表情包类型：1自定义图片 2 emoji
     */
    private Integer stickerType;
}
