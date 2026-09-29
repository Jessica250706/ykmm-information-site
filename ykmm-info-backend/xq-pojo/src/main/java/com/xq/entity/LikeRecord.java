package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 喜欢记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 1剧情对话句子 2RC对话句子 */
    private Integer targetType;

    /** dialogue_line.id */
    private Long targetId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
