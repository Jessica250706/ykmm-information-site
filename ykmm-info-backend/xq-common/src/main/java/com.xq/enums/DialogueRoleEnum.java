package com.xq.enums;

import lombok.Getter;

/**
 * 对话角色
 * 0-普通 1-问句 2-回答
 */
@Getter
public enum DialogueRoleEnum {

    /**
     * 普通对话
     */
    NORMAL(0, "普通"),

    /**
     * 选项问句
     */
    QUESTION(1, "问句"),

    /**
     * 选项回答
     */
    ANSWER(2, "回答");

    /**
     * 枚举值
     */
    private final Integer value;

    /**
     * 标签
     */
    private final String label;

    DialogueRoleEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据值查找枚举
     *
     * @param value 枚举值
     * @return 对应枚举，找不到返回 null
     */
    public static DialogueRoleEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (DialogueRoleEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据值取标签
     *
     * @param value 枚举值
     * @return 标签，无效时返回"普通"
     */
    public static String getLabel(Integer value) {
        DialogueRoleEnum e = of(value);
        return e == null ? NORMAL.label : e.label;
    }

    /**
     * 校验枚举值是否合法
     *
     * @param value 待校验值
     * @return true 合法
     */
    public static boolean isValid(Integer value) {
        return of(value) != null;
    }
}
