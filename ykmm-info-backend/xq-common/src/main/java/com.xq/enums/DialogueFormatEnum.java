package com.xq.enums;

import lombok.Getter;

/**
 * 对话形式
 */
@Getter
public enum DialogueFormatEnum {

    /**
     * 文字
     */
    TEXT(1, "文字"),

    /**
     * 图片
     */
    IMAGE(2, "图片");

    private final Integer value;
    private final String label;

    DialogueFormatEnum(Integer value, String label) {
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
        for (DialogueFormatEnum e : values()) {
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
        for (DialogueFormatEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
