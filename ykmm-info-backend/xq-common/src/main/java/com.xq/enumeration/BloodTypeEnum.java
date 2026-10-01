package com.xq.enumeration;

import lombok.Getter;

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
