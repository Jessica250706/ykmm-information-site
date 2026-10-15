package com.xq.service.impl;

import com.xq.constant.MessageConstant;
import com.xq.constant.UserRoleConstant;
import com.xq.constant.UserStatusConstant;
import com.xq.entity.SysUser;
import com.xq.exception.AccountLockedException;
import com.xq.exception.AccountNotFoundException;
import com.xq.exception.EmailAlreadyExistsException;
import com.xq.exception.PasswordErrorException;
import com.xq.mapper.SysUserMapper;
import com.xq.service.AuthService;
import com.xq.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REGISTER_CODE_KEY_PREFIX = "auth:register:code:";
    private static final String REGISTER_CODE_LIMIT_KEY_PREFIX = "auth:register:code:limit:";

    private static final Duration REGISTER_CODE_TTL = Duration.ofMinutes(5);
    private static final Duration REGISTER_CODE_SEND_LIMIT = Duration.ofSeconds(60);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SysUserMapper sysUserMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MailService mailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 发送注册验证码
     */
    @Override
    public void sendRegisterCode(String email) {
        email = normalizeEmail(email);

        if (email == null || email.isBlank()) {
            throw new RuntimeException("邮箱不能为空");
        }

        SysUser exist = sysUserMapper.getByEmail(email);
        if (exist != null) {
            throw new EmailAlreadyExistsException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        String limitKey = REGISTER_CODE_LIMIT_KEY_PREFIX + email;
        Boolean canSend = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", REGISTER_CODE_SEND_LIMIT);

        if (!Boolean.TRUE.equals(canSend)) {
            throw new RuntimeException("验证码发送过于频繁，请1分钟后再试");
        }

        String code = generateCode();
        String codeKey = REGISTER_CODE_KEY_PREFIX + email;

        // 先写 Redis，再异步发邮件
        stringRedisTemplate.opsForValue().set(codeKey, code, REGISTER_CODE_TTL);

        try {
            mailService.sendRegisterCode(email, code);
        } catch (Exception e) {
            // sendRegisterCode 已经 @Async，异常基本不会冒到这里
            log.error("触发邮件发送失败：email={}", email, e);
        }

        log.info("注册验证码已生成并投递：email={}", email);
    }

    /**
     * 注册，必须校验邮箱验证码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(String email, String password, String nickname, String code) {
        email = normalizeEmail(email);

        if (email == null || email.isBlank()) {
            throw new RuntimeException("邮箱不能为空");
        }

        String codeKey = REGISTER_CODE_KEY_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (cachedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }

        SysUser exist = sysUserMapper.getByEmail(email);
        if (exist != null) {
            throw new EmailAlreadyExistsException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        String uid = UUID.randomUUID().toString().replace("-", "");
        if (nickname == null || nickname.isBlank()) {
            nickname = email.substring(0, email.indexOf('@'));
        }

        SysUser user = SysUser.builder()
                .uid(uid)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .status(UserStatusConstant.ENABLED)
                .role(UserRoleConstant.USER)
                .build();

        sysUserMapper.insert(user);

        // 注册成功后删除验证码，防止重复使用
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(REGISTER_CODE_LIMIT_KEY_PREFIX + email);

        log.info("用户注册成功：email={}, userId={}", email, user.getId());
        return user;
    }

    @Override
    public SysUser login(String email, String password) {
        SysUser user = sysUserMapper.getByEmail(email);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        sysUserMapper.updateLoginInfo(user.getId(), null, LocalDateTime.now());
        log.info("用户登录成功：email={}, userId={}", email, user.getId());
        return user;
    }

    @Override
    public void logout(Long userId) {
        log.info("用户登出：userId={}", userId);
    }

    @Override
    public String refreshToken(Long userId) {
        SysUser user = sysUserMapper.getById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return null;
    }

    @Override
    public SysUser getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.getById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return user;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.getById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        String encoded = passwordEncoder.encode(newPassword);
        sysUserMapper.updatePassword(userId, encoded);
        log.info("密码修改成功：userId={}", userId);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
