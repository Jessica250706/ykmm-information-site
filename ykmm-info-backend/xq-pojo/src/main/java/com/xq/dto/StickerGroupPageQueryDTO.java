package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 表情包分组分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StickerGroupPageQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组名称关键字
     */
    private String name;
}