package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户答题-题目快照
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserQuizQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long attemptId;

    /**
     * 原题ID（仅参考）
     */
    private Long questionId;

    /**
     * 题干快照
     */
    private String questionContent;

    /**
     * 题型快照
     */
    private Integer questionType;

    /**
     * 是否固定题快照
     */
    private Integer isFixed;

    /**
     * 选项完整快照：[{"id":1,"content":"A","correct":true},...]
     */
    private String optionsSnapshot;

    /**
     * 本次展示的选项ID顺序，如 [3,1,4,2]
     */
    private String optionOrder;

    /**
     * 用户答案（选项ID列表），未答为 null
     */
    private String userAnswer;

    /**
     * 是否答对：0-否 1-是 null-未答
     */
    private Integer isCorrect;

    /**
     * 在本次答题中的顺序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
