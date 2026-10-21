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

    /**
     * 校验旧邮箱验证码（用于分步换绑的 Step 1）
     * <p>
     * 注意：校验成功不删除验证码，因为最终换绑时还需要再校验一次。
     *
     * @param userId 当前用户ID
     * @param code   用户输入的验证码
     */
    void verifyRebindOldCode(Long userId, String code);

    /**
     * 发送重置密码验证码到当前用户绑定邮箱
     *
     * @param userId 当前登录用户ID
     */
    void sendResetPasswordCode(Long userId);

    /**
     * 通过邮箱验证码重置密码（忘记原密码场景）
     *
     * @param userId      当前登录用户ID
     * @param code        邮箱验证码
     * @param newPassword 新密码
     */
    void resetPasswordByCode(Long userId, String code, String newPassword);
}
