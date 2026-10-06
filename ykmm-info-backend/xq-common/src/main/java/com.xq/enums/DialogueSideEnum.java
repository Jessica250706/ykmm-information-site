package com.xq.enums;

import lombok.Getter;

/**
 * 对话聊天左右位置
 * 1-左侧 2-右侧
 */
@Getter
public enum DialogueSideEnum {

    LEFT(1, "左侧"),
    RIGHT(2, "右侧");

    /** 枚举值 */
    private final Integer value;

    /** 标签 */
    private final String label;

    DialogueSideEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据值查找枚举
     *
     * @param value 枚举值
     * @return 对应枚举，找不到返回 null
     */
    public static DialogueSideEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (DialogueSideEnum e : values()) {
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
     * @return 标签，无效时返回"未知"
     */
    public static String getLabel(Integer value) {
        DialogueSideEnum e = of(value);
        return e == null ? "未知" : e.label;
    }

    /**
     * 校验枚举值是否合法
     *
     * @param value 待校验值
     * @return true 合法，false 不合法
     */
    public static boolean isValid(Integer value) {
        return of(value) != null;
    }
}
