package com.xq.service;

import com.xq.entity.SysUser;

public interface AuthService {

    /**
     * 发送注册邮箱验证码
     */
    void sendRegisterCode(String email);

    /**
     * 注册，必须带邮箱验证码
     */
    SysUser register(String email, String password, String nickname, String code);

    SysUser login(String email, String password);

    void logout(Long userId);

    String refreshToken(Long userId);

    SysUser getCurrentUser(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
