package com.xq.context;

import com.xq.constant.UserRoleConstant;
import com.xq.enums.StatusEnum;

import java.util.function.Consumer;

/**
 * 角色上下文工具
 * <p>
 * 三种身份：
 * - 游客（GUEST）：未登录，BaseContext.getCurrentRole() == null
 * - 用户（USER）：已登录，role = 2
 * - 管理员（ADMIN）：已登录，role = 1
 */
public final class RoleContext {

    private RoleContext() {
    }

    /* ============================================================
     * 身份判断
     * ============================================================ */

    /**
     * 是否游客（未登录）
     */
    public static boolean isGuest() {
        return BaseContext.getCurrentRole() == null;
    }

    /**
     * 是否管理员
     */
    public static boolean isAdmin() {
        Integer role = BaseContext.getCurrentRole();
        return role != null && role == UserRoleConstant.ADMIN;
    }

    /**
     * 是否普通用户
     */
    public static boolean isUser() {
        Integer role = BaseContext.getCurrentRole();
        return role != null && role == UserRoleConstant.USER;
    }

    /* ============================================================
     * 查询过滤
     * ============================================================ */

    /**
     * 应用访客过滤规则：
     * - 管理员：不加任何限制
     * - 游客：只查已发布
     * - 用户：已发布 + 自己创建的
     *
     * <p>配合 Mapper XML 使用：</p>
     * <pre>{@code
     * <if test="status != null">
     *   AND status = #{status}
     * </if>
     * <if test="viewerId != null">
     *   AND (status = 1 OR creator_id = #{viewerId})
     * </if>
     * }</pre>
     *
     * @param setStatus   设置 status 的回调，例如 query::setStatus
     * @param setViewerId 设置 viewerId 的回调，例如 query::setViewerId
     */
    public static void applyViewerFilter(Consumer<Integer> setStatus,
                                         Consumer<Long> setViewerId) {
        // 管理员：不限制
        if (isAdmin()) {
            if (setStatus != null) setStatus.accept(null);
            if (setViewerId != null) setViewerId.accept(null);
            return;
        }

        // 游客：只看已发布
        if (isGuest()) {
            if (setStatus != null) setStatus.accept(StatusEnum.PUBLISHED.getValue());
            if (setViewerId != null) setViewerId.accept(null);
            return;
        }

        // 用户：已发布 + 自己创建的
        // 不设置 status，让 viewerId 分支接管
        if (setStatus != null) setStatus.accept(null);
        if (setViewerId != null) setViewerId.accept(BaseContext.getCurrentId());
    }

    /**
     * 只关心"游客强制已发布"这一条的场景（等价于不查自己内容的接口）。
     * 管理员和用户不覆盖调用方传入的 status。
     *
     * @param setStatus 状态设置回调
     */
    public static void restrictToPublishedIfGuest(Consumer<Integer> setStatus) {
        if (setStatus == null) return;
        if (isGuest()) {
            setStatus.accept(StatusEnum.PUBLISHED.getValue());
        }
    }
}
