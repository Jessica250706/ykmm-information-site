package com.xq.mapper;

import com.xq.dto.QuestionPageQueryDTO;
import com.xq.entity.SysQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目 Mapper
 */
@Mapper
public interface QuestionMapper {

    /**
     * 分页查询（PageHelper 自动处理分页）
     */
    List<SysQuestion> pageQuery(@Param("query") QuestionPageQueryDTO query);

    SysQuestion getById(@Param("id") Long id);

    int insert(SysQuestion question);

    int update(SysQuestion question);

    int deleteById(@Param("id") Long id);

    /**
     * 查询所有启用状态的固定题
     */
    List<SysQuestion> listFixedEnabled();

    /**
     * 查询所有启用状态的随机题
     */
    List<SysQuestion> listRandomEnabled();

    /**
     * 统计启用状态的固定题数量
     */
    int countFixedEnabled();

    /**
     * 统计启用状态的随机题数量
     */
    int countRandomEnabled();

    /**
     * 统计每题的选项数量（列表展示用）
     */
    int countOptionsByQuestionId(@Param("questionId") Long questionId);
}
