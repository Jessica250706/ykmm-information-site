package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目列表项 VO（不含选项详情）
 */
@Data
public class QuestionListVO implements Serializable {

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
     * 选项数
     */
    private Integer optionCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
