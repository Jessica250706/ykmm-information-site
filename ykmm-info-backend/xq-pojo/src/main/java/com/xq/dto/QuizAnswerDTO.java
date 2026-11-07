package com.xq.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单题答案 DTO
 */
@Data
public class QuizAnswerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /**
     * 用户选择的选项ID列表（多选时多个）
     */
    @NotNull(message = "答案不能为空")
    private List<Long> optionIds;
}
