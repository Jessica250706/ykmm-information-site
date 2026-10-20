package com.xq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 换绑邮箱 - 校验旧邮箱验证码
 */
@Data
public class VerifyRebindOldCodeDTO {

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "验证码为 6 位数字")
    private String code;
}
