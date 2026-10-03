package com.xq.enums;

import lombok.Getter;

/**
 * 对话语言
 */
@Getter
public enum DialogueLanguageEnum {

    /**
     * 中文
     */
    CN(1, "中文"),

    /**
     * 日文
     */
    JP(2, "日文");

    private final Integer value;
    private final String label;

    DialogueLanguageEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 校验值是否合法
     *
     * @param value 值
     * @return 是否合法
     */
    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (DialogueLanguageEnum e : values()) {
            if (e.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取标签
     *
     * @param value 值
     * @return 标签
     */
    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (DialogueLanguageEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
