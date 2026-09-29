package com.xq.exception;

/**
 * 邮箱已注册异常
 */
public class EmailAlreadyExistsException extends BaseException {

  public EmailAlreadyExistsException() {
  }

  public EmailAlreadyExistsException(String msg) {
    super(msg);
  }

}
