package com.xq.enums;

import lombok.Getter;

/**
 * 卡面服装类型
 */
@Getter
public enum CardCostumeTypeEnum {
    CHIBI(1, "偶像小人"),
    MODEL_3D(2, "3D造型");

    private final Integer value;
    private final String label;

    CardCostumeTypeEnum(Integer value, String label) { this.value = value; this.label = label; }

    /** 根据值取标签 */
    public static String getLabel(Integer value) {
        for (CardCostumeTypeEnum e : values()) if (e.value.equals(value)) return e.label;
        return "未知";
    }
    /** 校验 */
    public static boolean isValid(Integer value) {
        for (CardCostumeTypeEnum e : values()) if (e.value.equals(value)) return true;
        return false;
    }
}
