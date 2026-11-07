package com.xq.service;

import com.xq.dto.QuestionDTO;
import com.xq.dto.QuestionPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.QuestionListVO;
import com.xq.vo.QuestionVO;

/**
 * 题目服务
 */
public interface QuestionService {

    /**
     * 分页查询（管理端）
     */
    PageResult<QuestionListVO> pageQuery(QuestionPageQueryDTO query);

    /**
     * 题目详情（管理端，含选项和正确答案）
     */
    QuestionVO getById(Long id);

    /**
     * 新增题目
     */
    Long create(QuestionDTO dto);

    /**
     * 编辑题目
     */
    void update(Long id, QuestionDTO dto);

    /**
     * 删除题目
     */
    void delete(Long id);
}
