package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 答题全局配置（单行，id 固定为 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysQuizConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
