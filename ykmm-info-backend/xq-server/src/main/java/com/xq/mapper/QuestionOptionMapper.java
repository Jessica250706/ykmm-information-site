package com.xq.mapper;

import com.xq.entity.SysQuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目-选项 Mapper
 */
@Mapper
public interface QuestionOptionMapper {

    List<SysQuestionOption> listByQuestionId(@Param("questionId") Long questionId);

    int insertBatch(@Param("options") List<SysQuestionOption> options);

    int deleteByQuestionId(@Param("questionId") Long questionId);
}
