package com.xq.controller.auth;

import com.xq.constant.JwtClaimsConstant;
import com.xq.context.BaseContext;
import com.xq.dto.LoginRequest;
import com.xq.dto.PasswordRequest;
import com.xq.dto.RegisterRequest;
import com.xq.dto.SendRegisterCodeRequest;
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
 * 认证
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
     * 发送注册邮箱验证码
     */
    @PostMapping("/register/code")
    public Result<?> sendRegisterCode(@RequestBody SendRegisterCodeRequest req) {
        log.info("发送注册验证码：{}", req.getEmail());
        authService.sendRegisterCode(req.getEmail());
        return Result.success();
    }

    /**
     * 用户注册，必须带邮箱验证码
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest req) {
        log.info("用户注册：{}", req.getEmail());

        SysUser user = authService.register(
                req.getEmail(),
                req.getPassword(),
                req.getNickname(),
                req.getCode()
        );

        String token = tokenService.createToken(user.getId(), user.getRole());

        return Result.success(LoginResponse.builder()
                .token(token)
                .user(UserInfo.from(user))
                .build());
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        log.info("用户登录：{}", req.getEmail());
        SysUser user = authService.login(req.getEmail(), req.getPassword());
        String token = tokenService.createToken(user.getId(), user.getRole());
        return Result.success(LoginResponse.builder()
                .token(token)
                .user(UserInfo.from(user))
                .build());
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户登出：userId={}", userId);
        authService.logout(userId);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh() {
        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();
        log.info("刷新 token：userId={}，role={}", userId, role);
        String newToken = authService.refreshToken(userId);
        if (newToken == null) {
            newToken = tokenService.createToken(userId, role);
        }
        Map<String, String> data = new HashMap<>();
        data.put(JwtClaimsConstant.TOKEN, newToken);
        return Result.success(data);
    }

    @GetMapping("/me")
    public Result<UserInfo> me() {
        Long userId = BaseContext.getCurrentId();
        log.info("userId={}", userId);
        SysUser user = authService.getCurrentUser(userId);
        return Result.success(UserInfo.from(user));
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody PasswordRequest req) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改密码：userId={}", userId);
        authService.changePassword(userId, req.getOldPassword(), req.getNewPassword());
        return Result.success();
    }
}
