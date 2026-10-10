package com.xq.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 表情包分组新增/更新DTO
 */
@Data
public class StickerGroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，更新时必填
     */
    private Long id;

    /**
     * 分组名称
     */
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 64, message = "分组名称不能超过64个字符")
    private String name;

    /**
     * 分组描述
     */
    @Size(max = 255, message = "分组描述不能超过255个字符")
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
