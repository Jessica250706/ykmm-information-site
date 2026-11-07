package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交答题结果 VO
 */
@Data
public class QuizSubmitResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long attemptId;

    /**
     * 状态：0-进行中 1-已通过 2-未通过
     */
    private Integer status;

    /**
     * 状态标签：0-进行中 1-已通过 2-未通过
     */
    private String statusLabel;

    /**
     * 实际答对数
     */
    private Integer correctCount;

    /**
     * 本次总题数（配置快照）
     */
    private Integer totalCount;

    /**
     * 本次通过所需题数（配置快照）
     */
    private Integer passCount;

    /**
     * 是否通过
     */
    private Boolean passed;

    private List<QuestionResult> questions;

    @Data
    public static class QuestionResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long questionId;
        private String content;
        private Boolean isCorrect;
        private List<Long> userAnswer;
        private List<Long> correctAnswer;
        private String explanation;
    }
}
