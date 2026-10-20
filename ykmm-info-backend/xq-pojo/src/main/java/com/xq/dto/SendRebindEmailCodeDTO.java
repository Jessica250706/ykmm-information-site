package com.xq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送换绑邮箱验证码
 */
@Data
public class SendRebindEmailCodeDTO {

    /** 目标类型：OLD 发到旧邮箱 / NEW 发到新邮箱 */
    @NotBlank(message = "目标类型不能为空")
    private String target;

    /** 目标邮箱（NEW 时必填；OLD 时忽略，后端强制用当前用户邮箱） */
    @Email(message = "邮箱格式不正确")
    private String email;
}
