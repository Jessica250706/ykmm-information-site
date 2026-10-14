package com.xq.exception;

import com.xq.constant.MessageConstant;

/**
 * 无权限异常
 *
 * 场景：
 * - 已登录，但角色不满足接口要求
 * - 非创建者试图修改 / 删除他人内容
 *
 * 一般对应 HTTP 403
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(MessageConstant.FORBIDDEN);
    }

    public ForbiddenException(String msg) {
        super(msg);
    }
}
