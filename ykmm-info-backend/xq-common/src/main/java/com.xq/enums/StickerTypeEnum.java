package com.xq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表情包类型枚举
 */
@Getter
@AllArgsConstructor
public enum StickerTypeEnum {

    /** 自定义图片 */
    CUSTOM_IMAGE(1, "自定义图片"),

    /** emoji */
    EMOJI(2, "emoji");

    /**
     * 枚举值。
     * <p>
     * 字段名必须叫 value：@Getter 会生成 getValue()，
     * 这是 @EnumValue 校验器通过反射寻找的方法名。
     */
    private final Integer value;

    /** 描述标签 */
    private final String label;

    /**
     * 根据 value 获取枚举
     */
    public static StickerTypeEnum fromValue(Integer value) {
        if (value == null) return null;
        for (StickerTypeEnum e : values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /* ==================== 兼容旧调用（迁移完成后可删） ==================== */

    /**
     * @deprecated 用 {@link #fromValue(Integer)} 替代
     */
    @Deprecated
    public static StickerTypeEnum fromCode(Integer code) {
        return fromValue(code);
    }

    /**
     * @deprecated 用 {@link #getValue()} 替代
     */
    @Deprecated
    public Integer getCode() {
        return this.value;
    }

    /**
     * @deprecated 用 {@link #getLabel()} 替代
     */
    @Deprecated
    public String getDesc() {
        return this.label;
    }
}
