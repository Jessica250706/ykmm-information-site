package com.xq.enums;

/**
 * 对话是否内心独白
 * 0-说出来的话 1-内心独白
 */
public enum DialogueMonologueEnum {

    SPOKEN(0, "说出来的话"),
    INNER(1, "内心独白");

    private final Integer value;
    private final String label;

    DialogueMonologueEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 安全取文案
     */
    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (DialogueMonologueEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return "未知";
    }

    /**
     * 是否内心独白
     */
    public static boolean isInner(Integer value) {
        return INNER.value.equals(value);
    }

    /**
     * 是否说出来的话（含 null 兼容）
     */
    public static boolean isSpoken(Integer value) {
        return value == null || SPOKEN.value.equals(value);
    }
}
