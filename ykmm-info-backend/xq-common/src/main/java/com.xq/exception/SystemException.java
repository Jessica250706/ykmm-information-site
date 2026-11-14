package com.xq.exception;

/**
 * 系统内部异常：非用户操作触发，属于程序 bug 或基础设施故障。
 * <p>
 * 与 {@link BusinessException} 的区别：
 * <ul>
 *   <li>{@link BusinessException}：业务规则校验失败，msg 直接展示给用户</li>
 *   <li>{@code SystemException}：系统内部错误，msg 仅供日志排查，
 *       前端应展示通用提示（如"系统繁忙，请稍后重试"）</li>
 * </ul>
 * <p>
 * 典型场景：反射调用失败、枚举类约定破坏、JSON 序列化失败等。
 */
public class SystemException extends BaseException {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
