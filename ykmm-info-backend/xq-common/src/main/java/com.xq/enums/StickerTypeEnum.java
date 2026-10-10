package com.xq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表情包类型枚举
 */
@Getter
@AllArgsConstructor
public enum StickerTypeEnum {

    /**
     * 自定义图片
     */
    CUSTOM_IMAGE(1, "自定义图片"),

    /**
     * emoji
     */
    EMOJI(2, "emoji");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static StickerTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StickerTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
