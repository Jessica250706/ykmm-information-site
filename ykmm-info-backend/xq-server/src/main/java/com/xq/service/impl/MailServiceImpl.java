package com.xq.service.impl;

import com.xq.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送注册验证码
     */
    @Async("mailExecutor")
    @Override
    public void sendRegisterCode(String to, String code) {
        String subject = "注册验证码";
        String text = "您正在注册账号，验证码为：" + code + "，5分钟内有效。请勿泄露给他人。";
        doSend(to, subject, text, "注册验证码");
    }

    /**
     * 发送通用验证码邮件
     * <p>
     * 可用于换绑邮箱、找回密码等场景，标题由调用方传入。
     */
    @Async("mailExecutor")
    @Override
    public void sendEmailCode(String to, String code, String title) {
        String subject = title;
        String text = "您正在进行「" + title + "」操作，验证码为：" + code + "，5分钟内有效。请勿泄露给他人。";
        doSend(to, subject, text, title);
    }

    /**
     * 同步发送逻辑（被两个 @Async 方法内部调用）
     * <p>
     * 说明：这里故意抽成 private 方法，而不是再挂 @Async。
     * 因为 Spring 的 @Async 基于代理，类内部 this.method() 调用不会走代理，
     * 挂 @Async 也无效；外层两个方法已经是异步的，doSend 同步执行是正确姿势。
     *
     * @param to      收件人
     * @param subject 邮件标题
     * @param text    邮件正文
     * @param logTag  日志标识（方便日志中区分「注册验证码」「换绑邮箱验证码」等）
     */
    private void doSend(String to, String subject, String text, String logTag) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("{}邮件已发送：to={}", logTag, to);
        } catch (Exception e) {
            log.error("{}邮件发送失败：to={}", logTag, to, e);
            // 异步线程里不 rethrow，避免异常被吞进线程池
            // 如需补偿：在这里清理 Redis 验证码，或落库重试
        }
    }
}
