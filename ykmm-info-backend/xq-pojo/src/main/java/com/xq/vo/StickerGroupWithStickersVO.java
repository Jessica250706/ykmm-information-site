package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 表情包分组（含下属表情包），用于表情选择器
 */
@Data
public class StickerGroupWithStickersVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分组ID */
    private Long id;

    /** 分组名称 */
    private String name;

    /** 分组描述 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 该分组下的表情包 */
    private List<StickerVO> stickers;
}
