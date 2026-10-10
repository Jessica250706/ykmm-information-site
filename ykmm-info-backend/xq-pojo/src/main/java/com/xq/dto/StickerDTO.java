package com.xq.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 表情包新增/更新DTO
 */
@Data
public class StickerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，更新时必填
     */
    private Long id;

    /**
     * 所属分组ID
     */
    @NotNull(message = "分组ID不能为空")
    private Long groupId;

    /**
     * 标签
     */
    @NotBlank(message = "标签不能为空")
    @Size(max = 64, message = "标签不能超过64个字符")
    private String label;

    /**
     * 图片地址
     */
    @Size(max = 512, message = "图片地址不能超过512个字符")
    private String imageUrl;

    /**
     * 对应emoji
     */
    @Size(max = 32, message = "emoji不能超过32个字符")
    private String emoji;

    /**
     * 1自定义图片 2 emoji
     */
    @NotNull(message = "表情包类型不能为空")
    private Integer stickerType;
}
