package com.xq.constant;

public class SysMessageConstant {
    /** 消息类型 */
    public static final int TYPE_REVIEW_PASSED = 1;
    public static final int TYPE_REVIEW_REJECTED = 2;
    public static final int TYPE_SYSTEM = 3;

    /** 关联业务类型 */
    public static final int TARGET_CARD = 1;
    public static final int TARGET_DIALOGUE_VERSION = 2;
    public static final int TARGET_STORY = 3;

    /** 已读状态：0-未读 1-已读 */
    public static final int READ_STATUS_UNREAD = 0;
    public static final int READ_STATUS_READ = 1;
}
