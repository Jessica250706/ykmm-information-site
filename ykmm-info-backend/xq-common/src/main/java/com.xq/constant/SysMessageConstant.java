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

    /** 广播角色：1-广播给管理员 */
    public static final int BROADCAST_ROLE_ADMIN = 1;

    /** 广播角色标签映射（用于 toVO） */
    public static String broadcastRoleLabel(Integer role) {
        if (role == null) return null;
        if (role == BROADCAST_ROLE_ADMIN) return "管理员广播";
        return null;
    }
}
