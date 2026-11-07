package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目详情 VO（含选项）
 */
@Data
public class QuestionVO implements Serializable {

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
     * 题型标签：1-单选 2-多选
     */
    private String questionTypeLabel;

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
     * 状态标签：1-启用 0-禁用
     */
    private String statusLabel;

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

    /**
     * 选项
     */
    private List<QuestionOptionVO> options;
}
