package com.xq.service.impl;

import com.xq.constant.MessageConstant;
import com.xq.entity.SysUser;
import com.xq.exception.AccountLockedException;
import com.xq.exception.AccountNotFoundException;
import com.xq.exception.EmailAlreadyExistsException;
import com.xq.exception.PasswordErrorException;
import com.xq.mapper.SysUserMapper;
import com.xq.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final SysUserMapper sysUserMapper;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SysUser register(String email, String password, String nickname) {
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
        .status(1)
        .build();

    sysUserMapper.insert(user);
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
}
