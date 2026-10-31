package com.xq.enums;

import com.xq.constant.NoticeVisibilityConstant;
import lombok.Getter;

/**
 * 公告可见性
 */
@Getter
public enum NoticeVisibilityEnum {

    GUEST(NoticeVisibilityConstant.GUEST, "所有人可见"),
    USER(NoticeVisibilityConstant.USER, "仅登录用户可见"),
    ADMIN(NoticeVisibilityConstant.ADMIN, "仅管理员可见");

    private final Integer value;
    private final String label;

    NoticeVisibilityEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) return false;
        for (NoticeVisibilityEnum e : values()) {
            if (e.value.equals(value)) return true;
        }
        return false;
    }

    public static String getLabel(Integer value) {
        if (value == null) return null;
        for (NoticeVisibilityEnum e : values()) {
            if (e.value.equals(value)) return e.label;
        }
        return null;
    }
}
