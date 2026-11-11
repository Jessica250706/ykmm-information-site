package com.xq.exception;

/**
 * 通用业务异常：用于业务规则校验失败（如重名、状态不允许等）
 * 由 GlobalExceptionHandler#handleBase 统一处理，message 原样返回前端
 */
public class BusinessException extends BaseException {
  public BusinessException(String message) {
    super(message);
  }
}
