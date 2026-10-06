package com.xq.enums;

import lombok.Getter;

/**
 * 卡面最高等级
 */
@Getter
public enum CardMaxRarityEnum {
    SSR(1, "SSR"),
    UR(2, "UR");

    private final Integer value;
    private final String label;

    CardMaxRarityEnum(Integer value, String label) { this.value = value; this.label = label; }

    /** 根据值取标签 */
    public static String getLabel(Integer value) {
        for (CardMaxRarityEnum e : values()) if (e.value.equals(value)) return e.label;
        return "未知";
    }
    /** 校验 */
    public static boolean isValid(Integer value) {
        for (CardMaxRarityEnum e : values()) if (e.value.equals(value)) return true;
        return false;
    }
}
