package com.xq.controller.user;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.context.BaseContext;
import com.xq.dto.*;
import com.xq.entity.SysUser;
import com.xq.result.Result;
import com.xq.service.AuthService;
import com.xq.service.UserService;
import com.xq.vo.UserInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 用户管理
 */
@RestController("UserUserController")
@RequestMapping("/user/user")
@RequireRole({UserRoleConstant.ADMIN, UserRoleConstant.USER})
@Slf4j
@Validated
public class UserUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserInfo> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 编辑用户
     */
    @PutMapping("/{id}")
    public Result<UserInfo> update(@PathVariable @NotNull(message = "用户ID不能为空") Long id,
                               @RequestBody @NotNull(message = "修改内容不能为空") UserEditDTO dto) {
        return Result.success(userService.update(id, dto));
    }

    /**
     * 发送换绑邮箱验证码
     */
    @PostMapping("/email/rebind/send-code")
    public Result<Void> sendRebindEmailCode(@RequestBody @Valid SendRebindEmailCodeDTO dto) {
        authService.sendRebindEmailCode(BaseContext.getCurrentId(), dto.getTarget(), dto.getEmail());
        return Result.success();
    }

    /**
     * 换绑邮箱
     */
    @PutMapping("/email/rebind")
    public Result<UserInfo> rebindEmail(@RequestBody @Valid RebindEmailDTO dto) {
        SysUser user = authService.rebindEmail(
                BaseContext.getCurrentId(), dto.getNewEmail(), dto.getOldCode(), dto.getNewCode());
        return Result.success(UserInfo.from(user));
    }

    /**
     * 换绑邮箱 - 校验旧邮箱验证码
     */
    @PostMapping("/email/rebind/verify-old")
    public Result<Void> verifyOldEmailCode(@RequestBody @Valid VerifyRebindOldCodeDTO dto) {
        authService.verifyRebindOldCode(BaseContext.getCurrentId(), dto.getCode());
        return Result.success();
    }

    /**
     * 修改密码（需原密码）
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        authService.changePassword(BaseContext.getCurrentId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    /**
     * 发送重置密码验证码（忘记原密码场景）
     */
    @PostMapping("/password/reset-code")
    public Result<Void> sendResetPasswordCode() {
        authService.sendResetPasswordCode(BaseContext.getCurrentId());
        return Result.success();
    }

    /**
     * 通过邮箱验证码重置密码（忘记原密码场景）
     */
    @PutMapping("/password/reset")
    public Result<Void> resetPasswordByCode(@RequestBody @Valid ResetPasswordDTO dto) {
        authService.resetPasswordByCode(BaseContext.getCurrentId(), dto.getCode(), dto.getNewPassword());
        return Result.success();
    }
}
