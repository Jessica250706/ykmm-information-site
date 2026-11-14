package com.xq.exception;

/**
 * 序列化 / 反序列化异常。
 * <p>
 * 场景：JSON 序列化或反序列化失败，通常是程序 bug 或数据损坏，与用户操作无关。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>继承 {@link SystemException}，由 GlobalExceptionHandler#handleBase 统一处理，避免落到"未知错误"；</li>
 *   <li>保留原始 cause 落日志，方便定位是哪个对象/字段出的问题；</li>
 *   <li>message 给中性文案（"数据序列化失败"），不向用户暴露 JSON 库的具体报错。</li>
 * </ul>
 */
public class SerializationException extends SystemException {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
