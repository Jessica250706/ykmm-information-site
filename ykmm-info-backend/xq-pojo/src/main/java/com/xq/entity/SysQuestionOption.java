package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库-选项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysQuestionOption implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 所属题目
     */
    private Long questionId;

    /**
     * 选项内容
     */
    private String content;

    /**
     * 是否正确答案：0-否 1-是
     */
    private Integer isCorrect;

    /**
     * 原始排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
