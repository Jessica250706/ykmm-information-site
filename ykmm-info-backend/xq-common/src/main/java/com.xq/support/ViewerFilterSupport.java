package com.xq.support;

import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.enums.StatusEnum;
import com.xq.exception.ForbiddenException;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 访客过滤支撑类
 * <p>
 * 提供两类通用逻辑：
 * 1. 分页查询：把身份转换为查询条件（status / viewerId）
 * 2. 单条详情：判断当前身份能否看到某条记录
 */
public final class ViewerFilterSupport {

    private ViewerFilterSupport() {
    }

    /* ============================================================
     * 分页：解析查询条件
     * ============================================================ */

    /**
     * 应用分页查询的身份过滤，返回传给 Mapper 的 viewerId
     *
     * @param setStatus 设置 status 的回调，例如 query::setStatus
     * @return viewerId
     */
    public static Long apply(Consumer<Integer> setStatus) {
        if (RoleContext.isAdmin()) {
            return null;
        }
        if (RoleContext.isGuest()) {
            if (setStatus != null) setStatus.accept(StatusEnum.PUBLISHED.getValue());
            return null;
        }
        if (setStatus != null) setStatus.accept(null);
        return BaseContext.getCurrentId();
    }

    /* ============================================================
     * 详情：判断单条记录可见性
     * ============================================================ */

    /**
     * 判断当前身份能否看到某条记录
     * <p>
     * - 管理员：全部可见
     * - 游客：仅已发布
     * - 用户：已发布 + 自己创建的
     *
     * @param status    记录状态
     * @param creatorId 记录创建者ID
     * @return 是否可见
     */
    public static boolean canView(Integer status, Long creatorId) {
        // 管理员：全部可见
        if (RoleContext.isAdmin()) {
            return true;
        }

        // 已发布：游客和用户都可看
        if (Objects.equals(status, StatusEnum.PUBLISHED.getValue())) {
            return true;
        }

        // 未发布：仅创建者本人可见
        return RoleContext.isUser()
                && Objects.equals(creatorId, BaseContext.getCurrentId());
    }

    /**
     * 校验可见性，不可见时抛 ForbiddenException
     * <p>
     * 注意：返回的提示统一为"记录不存在"，避免通过 id 枚举探测他人待审核内容
     *
     * @param status    记录状态
     * @param creatorId 记录创建者ID
     */
    public static void checkCanView(Integer status, Long creatorId) {
        if (!canView(status, creatorId)) {
            throw new ForbiddenException("记录不存在或无权查看");
        }
    }
}
