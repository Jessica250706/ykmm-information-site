package com.xq.enums;

import lombok.Getter;

/**
 * 卡面属性
 */
@Getter
public enum CardAttributeEnum {
    SHOUT(1, "Shout"),
    BEAT(2, "Beat"),
    MELODY(3, "Melody");

    private final Integer value;
    private final String label;

    CardAttributeEnum(Integer value, String label) { this.value = value; this.label = label; }

    /** 根据值取标签 */
    public static String getLabel(Integer value) {
        for (CardAttributeEnum e : values()) if (e.value.equals(value)) return e.label;
        return "未知";
    }
    /** 校验 */
    public static boolean isValid(Integer value) {
        for (CardAttributeEnum e : values()) if (e.value.equals(value)) return true;
        return false;
    }
}
