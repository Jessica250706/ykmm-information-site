package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题会话 VO
 */
@Data
public class QuizAttemptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long attemptId;

    /**
     * 本次总题数（配置快照）
     */
    private Integer totalCount;

    /**
     * 本次通过所需题数（配置快照）
     */
    private Integer passCount;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 问题
     */
    private List<QuizQuestionVO> questions;
}
