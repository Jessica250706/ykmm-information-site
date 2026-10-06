package com.xq.enums;

import lombok.Getter;

/**
 * 人物类型
 * 1-偶像 2-经纪人
 */
@Getter
public enum PersonTypeEnum {

    IDOL(1, "偶像"),
    MANAGER(2, "经纪人");

    private final Integer value;
    private final String label;

    PersonTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 校验是否合法
     *
     * @param value 枚举值
     * @return 是否合法
     */
    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (PersonTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全取文案
     *
     * @param value 枚举值
     * @return 文案，无效值返回 null
     */
    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (PersonTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }
}
