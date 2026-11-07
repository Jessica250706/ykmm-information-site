package com.xq.enums;

import com.xq.constant.QuizAttemptStatusConstant;
import lombok.Getter;

/**
 * 答题尝试状态
 */
@Getter
public enum QuizAttemptStatusEnum {

    IN_PROGRESS(QuizAttemptStatusConstant.IN_PROGRESS, "进行中"),
    PASSED(QuizAttemptStatusConstant.PASSED, "已通过"),
    FAILED(QuizAttemptStatusConstant.FAILED, "未通过");

    private final Integer value;
    private final String label;

    QuizAttemptStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static String getLabel(Integer value) {
        if (value == null) return null;
        for (QuizAttemptStatusEnum e : values()) {
            if (e.value.equals(value)) return e.label;
        }
        return null;
    }
}
