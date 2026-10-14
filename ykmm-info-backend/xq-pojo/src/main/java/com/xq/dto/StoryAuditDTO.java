package com.xq.dto;

import com.xq.constant.StatusConstant;
import com.xq.constant.StoryConstant;
import com.xq.validation.InIntValues;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 剧情审核
 */
@Data
public class StoryAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核结果：1通过 3拒绝
     */
    @NotNull(message = "审核结果不能为空")
    @InIntValues(value = {StatusConstant.PUBLISHED, StatusConstant.REJECTED}, message = "审核结果只能是已发布或已拒绝")
    private Integer status;

    /**
     * 审核备注
     */
    @Size(max = StoryConstant.REVIEW_REMARK_MAX_LENGTH,
            message = "审核备注长度不能超过 " + StoryConstant.REVIEW_REMARK_MAX_LENGTH + " 个字符")
    private String reviewRemark;
}
