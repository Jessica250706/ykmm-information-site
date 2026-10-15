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

    @Async("mailExecutor")
    @Override
    public void sendRegisterCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("注册验证码");
            message.setText("您正在注册账号，验证码为：" + code + "，5分钟内有效。请勿泄露给他人。");
            mailSender.send(message);
            log.info("注册验证码邮件已发送：to={}", to);
        } catch (Exception e) {
            log.error("注册验证码邮件发送失败：to={}", to, e);
            // 这里不建议再 throw，异步线程抛出去没人接收
            // 如果需要补偿，可以在这里删除 Redis 验证码，或落库重试
        }
    }
}
