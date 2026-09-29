package com.xq.controller.auth;

import com.xq.context.BaseContext;
import com.xq.dto.LoginRequest;
import com.xq.dto.PasswordRequest;
import com.xq.dto.RegisterRequest;
import com.xq.entity.SysUser;
import com.xq.result.Result;
import com.xq.service.AuthService;
import com.xq.service.TokenService;
import com.xq.vo.LoginResponse;
import com.xq.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证模块
 */
@RestController("AuthController")
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    /**
     * 用户注册
     *
     * @param req 注册请求参数，包含邮箱、密码和昵称
     * @return 登录响应，包含 token 和用户信息
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest req) {
        log.info("用户注册：{}", req.getEmail());
        SysUser user = authService.register(req.getEmail(), req.getPassword(), req.getNickname());
        String token = tokenService.createUserToken(user.getId());
        // TODO: 用户注册成功后，向对应邮箱发送邮件
        return Result.success(LoginResponse.builder()
                .token(token)
                .user(UserInfo.from(user))
                .build());
    }

    /**
     * 用户登录
     *
     * @param req 登录请求参数，包含邮箱和密码
     * @return 登录响应，包含 token 和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        log.info("用户登录：{}", req.getEmail());
        SysUser user = authService.login(req.getEmail(), req.getPassword());
        String token = tokenService.createUserToken(user.getId());
        return Result.success(LoginResponse.builder()
                .token(token)
                .user(UserInfo.from(user))
                .build());
    }

    /**
     * 用户登出
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户登出：userId={}", userId);
        authService.logout(userId);
        return Result.success();
    }

    /**
     * 刷新 token
     *
     * @return 包含新 token 的 Map
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh() {
        Long userId = BaseContext.getCurrentId();
        log.info("刷新 token：userId={}", userId);
        String newToken = authService.refreshToken(userId);
        if (newToken == null) {
            newToken = tokenService.createUserToken(userId);
        }
        Map<String, String> data = new HashMap<>();
        data.put("token", newToken);
        return Result.success(data);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserInfo> me() {
        Long userId = BaseContext.getCurrentId();
        log.info("userId={}", userId);
        SysUser user = authService.getCurrentUser(userId);
        return Result.success(UserInfo.from(user));
    }

    /**
     * 修改密码
     *
     * @param req 密码请求参数，包含旧密码和新密码
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody PasswordRequest req) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改密码：userId={}", userId);
        authService.changePassword(userId, req.getOldPassword(), req.getNewPassword());
        return Result.success();
    }
}
