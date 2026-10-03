package com.xq.enums;

import lombok.Getter;

/**
 * 对话范围
 */
@Getter
public enum DialogueScopeEnum {

    /**
     * 全部
     */
    ALL(1, "全部"),

    /**
     * 节选
     */
    PARTIAL(2, "节选");

    private final Integer value;
    private final String label;

    DialogueScopeEnum(Integer value, String label) {
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
        for (DialogueScopeEnum e : values()) {
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
        for (DialogueScopeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
