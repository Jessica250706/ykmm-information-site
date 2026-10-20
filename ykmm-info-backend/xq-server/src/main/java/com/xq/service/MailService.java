package com.xq.service;

public interface MailService {

    /**
     * 发送注册验证码
     *
     * @param to   收件邮箱
     * @param code 验证码
     */
    void sendRegisterCode(String to, String code);

    /**
     * 发送通用验证码邮件
     *
     * @param to    收件人邮箱
     * @param code  验证码
     * @param title 邮件标题 / 操作名称，如「换绑邮箱验证码」「找回密码验证码」
     */
    void sendEmailCode(String to, String code, String title);
}