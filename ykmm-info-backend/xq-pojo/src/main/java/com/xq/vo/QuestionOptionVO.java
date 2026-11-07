package com.xq.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目-选项 VO
 */
@Data
public class QuestionOptionVO implements Serializable {

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
     * 是否正确答案（管理端可见，用户答题时不返回）
     */
    private Integer isCorrect;

    /**
     * 排序
     */
    private Integer sort;
}
