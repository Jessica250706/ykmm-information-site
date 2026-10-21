package com.xq.enums;

import com.xq.constant.SysMessageConstant;
import lombok.Getter;

/**
 * 系统消息 - 关联业务类型
 * <p>
 * 与 {@link SysMessageConstant#TARGET_CARD} 等常量保持数值一致。
 */
@Getter
public enum SysMessageTargetTypeEnum {

    /** 卡面 */
    CARD(SysMessageConstant.TARGET_CARD, "卡面"),

    /** 对话版本 */
    DIALOGUE_VERSION(SysMessageConstant.TARGET_DIALOGUE_VERSION, "对话版本"),

    /** 剧情 */
    STORY(SysMessageConstant.TARGET_STORY, "剧情");

    private final Integer value;
    private final String label;

    SysMessageTargetTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (SysMessageTargetTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (SysMessageTargetTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }

    public static SysMessageTargetTypeEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (SysMessageTargetTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
