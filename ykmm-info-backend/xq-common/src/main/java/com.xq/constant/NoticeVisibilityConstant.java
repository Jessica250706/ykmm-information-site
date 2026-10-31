package com.xq.constant;

/**
 * 公告最低可见级别
 * <p>
 * 值越大范围越窄；查询时用 {@code currentVisibility >= minVisibility} 过滤。
 */
public class NoticeVisibilityConstant {
    /** 游客及以上（所有人可见） */
    public static final int GUEST = 1;
    /** 用户及以上（仅登录用户） */
    public static final int USER = 2;
    /** 仅管理员 */
    public static final int ADMIN = 3;
}
