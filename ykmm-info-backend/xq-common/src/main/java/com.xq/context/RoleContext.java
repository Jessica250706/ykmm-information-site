package com.xq.context;

import com.xq.constant.UserRoleConstant;
import com.xq.enums.StatusEnum;

import java.util.function.Consumer;

/**
 * 角色上下文工具
 */
public final class RoleContext {

    private RoleContext() {
    }

    /**
     * 获取当前用户角色，未登录或未设置时默认 ADMIN
     */
    public static int currentOrDefaultAdmin() {
        Integer role = BaseContext.getCurrentRole();
        return role == null ? UserRoleConstant.ADMIN : role;
    }

    /**
     * 当前用户是否管理员（未设置时视为管理员）
     */
    public static boolean isAdmin() {
        return currentOrDefaultAdmin() == UserRoleConstant.ADMIN;
    }

    /**
     * 当前用户是否普通用户（未设置时视为管理员 → 返回 false）
     */
    public static boolean isUser() {
        return currentOrDefaultAdmin() == UserRoleConstant.USER;
    }

    /**
     * 普通用户视角强制只查已发布内容。
     * 管理员不覆盖，保持调用方传入的查询条件。
     *
     * @param setStatus 状态设置回调，例如 query::setStatus
     */
    public static void restrictToPublishedIfUser(Consumer<Integer> setStatus) {
        if (setStatus == null) return;
        if (!isUser()) return;
        setStatus.accept(StatusEnum.PUBLISHED.getValue());
    }
}
