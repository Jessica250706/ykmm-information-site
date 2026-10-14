package com.xq.enums;

import com.xq.constant.UserRoleConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * - GUEST：未登录游客，无数据库记录，仅用于代码判断
 * - USER：普通用户，对应 sys_user.role = 2
 * - ADMIN：管理员，对应 sys_user.role = 1
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    /** 游客：未登录 */
    GUEST(UserRoleConstant.GUEST, "游客"),

    /** 管理员 */
    ADMIN(UserRoleConstant.ADMIN, "管理员"),

    /** 普通用户 */
    USER(UserRoleConstant.USER, "普通用户");

    private final Integer value;
    private final String label;

    /**
     * 根据数据库 role 值解析枚举
     */
    public static UserRoleEnum of(Integer value) {
        if (value == null) return GUEST;
        for (UserRoleEnum e : values()) {
            if (e.getValue().equals(value)) return e;
        }
        return GUEST;
    }

    /**
     * 从当前登录上下文解析角色（未登录返回 GUEST）
     */
    public static UserRoleEnum current() {
        Integer role = com.xq.context.BaseContext.getCurrentRole();
        return of(role);
    }

    /**
     * 是否至少为普通用户（即已登录）
     */
    public boolean isAtLeastUser() {
        return this != GUEST;
    }

    /**
     * 是否管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
}
