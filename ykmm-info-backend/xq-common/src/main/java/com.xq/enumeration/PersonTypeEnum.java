package com.xq.enumeration;

import lombok.Getter;

@Getter
public enum PersonTypeEnum {

    IDOL(1, "偶像"),
    MANAGER(2, "经纪人");

    private final Integer value;
    private final String label;

    PersonTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (PersonTypeEnum e : values()) {
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
        for (PersonTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
