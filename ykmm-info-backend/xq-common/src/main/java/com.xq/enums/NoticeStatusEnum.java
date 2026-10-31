package com.xq.enums;

import com.xq.constant.NoticeStatusConstant;
import lombok.Getter;

/**
 * 公告状态
 */
@Getter
public enum NoticeStatusEnum {

    DRAFT(NoticeStatusConstant.DRAFT, "草稿"),
    PUBLISHED(NoticeStatusConstant.PUBLISHED, "已发布"),
    OFFLINE(NoticeStatusConstant.OFFLINE, "已下线");

    private final Integer value;
    private final String label;

    NoticeStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) return false;
        for (NoticeStatusEnum e : values()) {
            if (e.value.equals(value)) return true;
        }
        return false;
    }

    public static String getLabel(Integer value) {
        if (value == null) return null;
        for (NoticeStatusEnum e : values()) {
            if (e.value.equals(value)) return e.label;
        }
        return null;
    }
}
