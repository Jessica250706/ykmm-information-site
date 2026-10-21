package com.xq.enums;

import com.xq.constant.SysMessageConstant;
import lombok.Getter;

/**
 * 系统消息类型
 * <p>
 * 与 {@link SysMessageConstant#TYPE_REVIEW_PASSED} 等常量保持数值一致。
 * 使用枚举而非 String[] 数组的好处：
 * <ul>
 *   <li>类型安全：Service 里传入 / 返回的都是枚举，不会出现"传了 5 但只有 3 个标签"的越界</li>
 *   <li>可读性强：代码里写 SysMessageTypeEnum.REVIEW_PASSED，比写 1 直观</li>
 *   <li>易扩展：新增类型只加一个枚举项，getLabel / isValid 自动覆盖</li>
 * </ul>
 */
@Getter
public enum SysMessageTypeEnum {

    /** 审核通过 */
    REVIEW_PASSED(SysMessageConstant.TYPE_REVIEW_PASSED, "审核通过"),

    /** 审核拒绝 */
    REVIEW_REJECTED(SysMessageConstant.TYPE_REVIEW_REJECTED, "审核拒绝"),

    /** 系统通知 */
    SYSTEM(SysMessageConstant.TYPE_SYSTEM, "系统通知");

    private final Integer value;
    private final String label;

    SysMessageTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 校验类型值是否合法
     */
    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (SysMessageTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据值取标签；不合法返回 null
     */
    public static String getLabel(Integer value) {
        if (value == null) {
            return null;
        }
        for (SysMessageTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e.label;
            }
        }
        return null;
    }

    /**
     * 根据值取枚举；不合法返回 null
     */
    public static SysMessageTypeEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (SysMessageTypeEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
