package com.xq.exception;

/**
 * 业务异常
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }

    /**
     * 保留原始 cause，便于日志排查。
     * <p>前端只展示 message，cause 仅落日志。
     */
    public BaseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
