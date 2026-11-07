package com.xq.mapper;

import com.xq.entity.SysUserQuizQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户答题-题目快照 Mapper
 */
@Mapper
public interface UserQuizQuestionMapper {

    int insertBatch(@Param("list") List<SysUserQuizQuestion> list);

    List<SysUserQuizQuestion> listByAttemptId(@Param("attemptId") Long attemptId);

    SysUserQuizQuestion getByAttemptAndQuestion(@Param("attemptId") Long attemptId,
                                                @Param("questionId") Long questionId);

    int update(SysUserQuizQuestion record);
}
