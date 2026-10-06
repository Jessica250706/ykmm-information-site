package com.xq.enums;

import lombok.Getter;

/**
 * 血型
 * 1-A 2-B 3-O 4-AB 5-其他
 */
@Getter
public enum BloodTypeEnum {

    A(1, "A"),
    B(2, "B"),
    O(3, "O"),
    AB(4, "AB"),
    OTHER(5, "其他");

    private final Integer value;
    private final String label;

    BloodTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (BloodTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全取文案
     *
     * @param value 枚举值
     * @return 文案，无效值返回 null
     */
    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (BloodTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
