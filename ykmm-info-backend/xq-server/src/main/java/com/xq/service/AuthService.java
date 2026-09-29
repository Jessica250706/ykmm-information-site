package com.xq.service;

import com.xq.entity.SysUser;

public interface AuthService {

    SysUser register(String email, String password, String nickname);

    SysUser login(String email, String password);

    void logout(Long userId);

    String refreshToken(Long userId);

    SysUser getCurrentUser(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
