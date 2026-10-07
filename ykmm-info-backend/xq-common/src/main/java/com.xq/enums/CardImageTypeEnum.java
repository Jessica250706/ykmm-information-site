package com.xq.enums;

import lombok.Getter;

/**
 * 卡面图片类型
 */
@Getter
public enum CardImageTypeEnum {
    R(1, "R"),
    SR(2, "SR"),
    SSR(3, "SSR"),
    SSR_HIDDEN(4, "SSR隐藏款"),
    UR_VERTICAL(5, "UR竖卡"),
    UR_HORIZONTAL(6, "UR横卡");

    private final Integer value;
    private final String label;

    CardImageTypeEnum(Integer value, String label) { this.value = value; this.label = label; }

    /** 根据值取标签 */
    public static String getLabel(Integer value) {
        for (CardImageTypeEnum e : values()) if (e.value.equals(value)) return e.label;
        return "未知";
    }
    /** 校验 */
    public static boolean isValid(Integer value) {
        for (CardImageTypeEnum e : values()) if (e.value.equals(value)) return true;
        return false;
    }
}