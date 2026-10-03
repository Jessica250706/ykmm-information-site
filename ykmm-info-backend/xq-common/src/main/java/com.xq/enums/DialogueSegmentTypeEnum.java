package com.xq.enums;

import lombok.Getter;

/**
 * 对话片段类型
 */
@Getter
public enum DialogueSegmentTypeEnum {

    /**
     * 文本
     */
    TEXT(1, "文本"),

    /**
     * 表情包
     */
    STICKER(2, "表情包");

    private final Integer value;
    private final String label;

    DialogueSegmentTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
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
        for (DialogueSegmentTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
