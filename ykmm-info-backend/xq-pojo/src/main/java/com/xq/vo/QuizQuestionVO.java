package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 答题中的题目 VO（选项已随机排序、不含正确答案）
 */
@Data
public class QuizQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原题ID（仅参考）
     */
    private Long questionId;

    /**
     * 题干快照
     */
    private String content;

    /**
     * 题型快照
     */
    private Integer questionType;

    /**
     * 是否固定题快照
     */
    private Integer isFixed;

    /**
     * 在本次答题中的顺序
     */
    private Integer sort;

    /**
     * 选项（按 option_order 排好，不含 isCorrect）
     */
    private List<OptionItem> options;

    @Data
    public static class OptionItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String content;
    }
}
