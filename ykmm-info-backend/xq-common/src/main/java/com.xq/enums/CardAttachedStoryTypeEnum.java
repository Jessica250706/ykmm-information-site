package com.xq.enums;

import lombok.Getter;

/**
 * 卡面附属剧情类型
 */
@Getter
public enum CardAttachedStoryTypeEnum {
    NONE(0, "无"),
    RC(1, "RC"),
    RTV(2, "RTV"),
    RABBITTER(3, "Rabbiter");

    private final Integer value;
    private final String label;

    CardAttachedStoryTypeEnum(Integer value, String label) { this.value = value; this.label = label; }

    /** 根据值取标签 */
    public static String getLabel(Integer value) {
        for (CardAttachedStoryTypeEnum e : values()) if (e.value.equals(value)) return e.label;
        return "未知";
    }
    /** 校验 */
    public static boolean isValid(Integer value) {
        for (CardAttachedStoryTypeEnum e : values()) if (e.value.equals(value)) return true;
        return false;
    }
}
