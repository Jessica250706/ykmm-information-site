package com.xq.service;

public interface MailService {

    /**
     * 发送注册验证码
     *
     * @param to   收件邮箱
     * @param code 验证码
     */
    void sendRegisterCode(String to, String code);
}