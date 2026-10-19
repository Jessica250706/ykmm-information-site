package com.xq.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 卡面审核请求
 */
@Data
public class CardReviewDTO {

    /**
     * 审核结果：
     * 1 - 通过（已发布）
     * 3 - 拒绝（已拒绝）
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 审核备注（拒绝时必填；通过时可选，用于留痕）
     */
    @Size(max = 255, message = "审核备注不能超过 255 字")
    private String reviewRemark;
}
