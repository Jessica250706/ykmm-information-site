package com.xq.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户答题状态 VO
 */
@Data
public class QuizStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否已通过
     */
    private Boolean quizPassed;
    /**
     * 答题是否开启
     */
    private Boolean enabled;
    /**
     * 是否有进行中的答题
     */
    private Boolean hasInProgress;
    /**
     * 进行中的 attemptId
     */
    private Long inProgressAttemptId;
    /**
     * 已用次数
     */
    private Integer attemptsUsed;
    /**
     * 最大次数（0=不限）
     */
    private Integer maxAttempts;
    /**
     * 是否还可以开始新答题
     */
    private Boolean canAttempt;
}
