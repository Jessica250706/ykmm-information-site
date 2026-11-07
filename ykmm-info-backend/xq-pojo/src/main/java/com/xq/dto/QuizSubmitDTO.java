package com.xq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交答题 DTO
 */
@Data
public class QuizSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "答题记录ID不能为空")
    private Long attemptId;

    @NotEmpty(message = "答案不能为空")
    @Valid
    private List<QuizAnswerDTO> answers;
}
