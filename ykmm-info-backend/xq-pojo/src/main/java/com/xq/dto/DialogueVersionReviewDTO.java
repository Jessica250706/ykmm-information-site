package com.xq.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 对话版本审核请求
 */
@Data
public class DialogueVersionReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核结果：
     * 1 - 通过（已发布）
     * 3 - 拒绝（已拒绝）
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 审核备注（拒绝时必填，通过时可选）
     */
    @Size(max = 255, message = "审核备注最多 255 个字符")
    private String reviewRemark;
}
