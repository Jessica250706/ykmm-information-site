package com.xq.exception;

/**
 * 答题未通过异常。
 * <p>
 * code = 423：前端识别后应跳转到 /quiz。
 */
public class QuizRequiredException extends RuntimeException {

    /** 业务 code：前端据此跳转答题页 */
    public static final int CODE = 423;

    public QuizRequiredException() {
        super("请先完成答题后再使用此功能");
    }

    public QuizRequiredException(String message) {
        super(message);
    }
}
