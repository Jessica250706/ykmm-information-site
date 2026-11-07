package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 题目分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionPageQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键词：题干模糊匹配
     */
    private String keyword;

    /**
     * 题型：1-单选 2-多选
     */
    private Integer questionType;

    /**
     * 是否固定题：0-随机 1-固定
     */
    private Integer isFixed;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
