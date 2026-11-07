package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户答题尝试记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserQuizAttempt implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    private Long userId;

    /**
     * 状态：0-进行中 1-已通过 2-未通过
     */
    private Integer status;

    /**
     * 本次总题数（配置快照）
     */
    private Integer totalCount;

    /**
     * 本次通过所需题数（配置快照）
     */
    private Integer passCount;

    /**
     * 实际答对数
     */
    private Integer correctCount;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
