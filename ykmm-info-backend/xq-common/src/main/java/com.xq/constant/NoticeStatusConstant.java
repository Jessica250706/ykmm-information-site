package com.xq.constant;

/**
 * 公告状态
 */
public class NoticeStatusConstant {
    /**
     * 草稿
     */
    public static final int DRAFT = 0;
    /**
     * 已发布
     */
    public static final int PUBLISHED = 1;
    /**
     * 已下线
     */
    public static final int OFFLINE = 2;
    /**
     * 待发布（定时发布，等待 publish_at 到达）
     */
    public static final int SCHEDULED = 3;
}
