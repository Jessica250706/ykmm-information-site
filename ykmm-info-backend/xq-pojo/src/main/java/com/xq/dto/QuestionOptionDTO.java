package com.xq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 题目-选项 DTO
 */
@Data
public class QuestionOptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选项ID（编辑时用于匹配；新增时为 null）
     */
    private Long id;

    @NotBlank(message = "选项内容不能为空")
    @Size(max = 500, message = "选项内容最多 500 个字符")
    private String content;

    /**
     * 是否正确答案：0-否 1-是
     */
    @NotNull(message = "选项正确性不能为空")
    private Integer isCorrect;

    /**
     * 排序
     */
    private Integer sort;
}
