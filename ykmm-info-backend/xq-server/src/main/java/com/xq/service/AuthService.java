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

    /**
     * 发送换绑邮箱验证码
     *
     * @param userId      当前登录用户ID
     * @param target      OLD 发旧邮箱 / NEW 发新邮箱
     * @param targetEmail NEW 时的目标邮箱；OLD 时后端强制用当前用户邮箱
     */
    void sendRebindEmailCode(Long userId, String target, String targetEmail);

    /**
     * 换绑邮箱（需同时验证旧邮箱、新邮箱验证码）
     *
     * @return 更新后的用户信息
     */
    SysUser rebindEmail(Long userId, String newEmail, String oldCode, String newCode);
}
