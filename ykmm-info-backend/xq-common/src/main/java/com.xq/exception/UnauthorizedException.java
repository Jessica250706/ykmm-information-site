package com.xq.exception;

import com.xq.constant.MessageConstant;

/**
 * 未认证异常
 *
 * 场景：
 * - 未携带 token
 * - token 过期 / 非法
 * - token 解析失败
 *
 * 一般对应 HTTP 401
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(MessageConstant.UNAUTHORIZED);
    }

    public UnauthorizedException(String msg) {
        super(msg);
    }
}
