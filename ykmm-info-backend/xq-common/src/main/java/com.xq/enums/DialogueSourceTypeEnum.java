package com.xq.enums;

import lombok.Getter;

/**
 * 对话来源类型
 */
@Getter
public enum DialogueSourceTypeEnum {

    /**
     * 无
     */
    NONE(0, "无"),

    /**
     * 卡面 RC
     */
    RC(1, "RC"),

    /**
     * 卡面 RTV
     */
    RTV(2, "RTV"),

    /**
     * 剧情
     */
    RABITTER(3, "Rabitter"),

    /**
     * 剧情
     */
    STORY(4, "剧情");

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
            return "未知";
        }
        for (DialogueSourceTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return "未知";
    }
}
