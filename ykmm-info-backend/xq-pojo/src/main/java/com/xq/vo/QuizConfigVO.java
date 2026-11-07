package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 答题配置 VO（含题目统计）
 */
@Data
public class QuizConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否开启答题门槛：1-开启 0-关闭
     */
    private Integer enabled;

    /**
     * 每次答题的总题数
     */
    private Integer totalCount;

    /**
     * 通过所需答对题数
     */
    private Integer passCount;

    /**
     * 最大答题次数：0-不限
     */
    private Integer maxAttempts;

    /**
     * 固定题数量（统计，只读）
     */
    private Integer fixedCount;

    /**
     * 随机题数量（统计，只读）
     */
    private Integer randomCount;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
