package com.xq.enums;

import lombok.Getter;

/**
 * 对话来源类型
 */
@Getter
public enum DialogueSourceTypeEnum {

    /**
     * 剧情
     */
    STORY(1, "剧情"),

    /**
     * 卡面 RTV
     */
    RTV(2, "卡面RTV"),

    /**
     * 卡面 RC
     */
    RC(3, "卡面RC");

    private final Integer value;
    private final String label;

    DialogueSourceTypeEnum(Integer value, String label) {
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
        for (DialogueSourceTypeEnum e : values()) {
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
        for (DialogueSourceTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
