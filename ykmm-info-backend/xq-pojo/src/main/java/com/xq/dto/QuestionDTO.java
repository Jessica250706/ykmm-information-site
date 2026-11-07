package com.xq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目新增/编辑 DTO
 */
@Data
public class QuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "题干不能为空")
    private String content;

    /**
     * 题型：1-单选 2-多选
     */
    @NotNull(message = "题型不能为空")
    private Integer questionType;

    /**
     * 是否固定题：0-随机 1-固定
     */
    @NotNull(message = "是否固定题不能为空")
    private Integer isFixed;

    /**
     * 答案解析（可选）
     */
    private String explanation;

    /**
     * 状态：1-启用 0-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 选项列表
     */
    @NotEmpty(message = "选项不能为空")
    @Valid
    private List<QuestionOptionDTO> options;
}
