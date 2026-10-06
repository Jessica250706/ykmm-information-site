package com.xq.enums;

import lombok.Getter;

/**
 * 审核状态
 */
@Getter
public enum StatusEnum {

    /**
     * 已发布
     */
    PUBLISHED(1, "已发布"),

    /**
     * 待审核
     */
    PENDING(2, "待审核"),

    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝");

    private final Integer value;
    private final String label;

    StatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 校验状态值是否合法
     *
     * @param value 状态值
     * @return 是否合法
     */
    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (StatusEnum e : values()) {
            if (e.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据值获取标签
     *
     * @param value 状态值
     * @return 标签
     */
    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (StatusEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
