package com.xq.exception;

/**
 * 重复/已存在异常：用于"记录已存在"类校验失败
 * <p>继承 {@link BaseException}，由 GlobalExceptionHandler#handleBase 统一处理，
 * message 原样返回前端。
 */
public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}