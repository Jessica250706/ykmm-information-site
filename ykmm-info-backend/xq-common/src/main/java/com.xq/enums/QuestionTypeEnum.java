package com.xq.enums;

import lombok.Getter;

/**
 * 题型
 */
@Getter
public enum QuestionTypeEnum {

    SINGLE(1, "单选"),
    MULTI(2, "多选");

    private final Integer value;
    private final String label;

    QuestionTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) return false;
        for (QuestionTypeEnum e : values()) {
            if (e.value.equals(value)) return true;
        }
        return false;
    }

    public static String getLabel(Integer value) {
        if (value == null) return null;
        for (QuestionTypeEnum e : values()) {
            if (e.value.equals(value)) return e.label;
        }
        return null;
    }
}
