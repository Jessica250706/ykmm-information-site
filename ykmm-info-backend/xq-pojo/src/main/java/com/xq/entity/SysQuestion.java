package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库-题目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 题干
     */
    private String content;

    /**
     * 题型：1-单选 2-多选
     */
    private Integer questionType;

    /**
     * 是否固定题：0-随机 1-固定
     */
    private Integer isFixed;

    /**
     * 答案解析（可选）
     */
    private String explanation;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
