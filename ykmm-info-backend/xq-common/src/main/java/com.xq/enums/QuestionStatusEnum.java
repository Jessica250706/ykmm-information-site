package com.xq.enums;

import com.xq.constant.QuestionStatusConstant;
import lombok.Getter;

/**
 * 题目状态
 */
@Getter
public enum QuestionStatusEnum {

    DISABLED(QuestionStatusConstant.DISABLED, "禁用"),
    ENABLED(QuestionStatusConstant.ENABLED, "启用");

    private final Integer value;
    private final String label;

    QuestionStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) return false;
        for (QuestionStatusEnum e : values()) {
            if (e.value.equals(value)) return true;
        }
        return false;
    }

    public static String getLabel(Integer value) {
        if (value == null) return null;
        for (QuestionStatusEnum e : values()) {
            if (e.value.equals(value)) return e.label;
        }
        return null;
    }
}
