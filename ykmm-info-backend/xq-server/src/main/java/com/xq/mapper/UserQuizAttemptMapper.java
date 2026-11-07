package com.xq.mapper;

import com.xq.entity.SysUserQuizAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户答题尝试 Mapper
 */
@Mapper
public interface UserQuizAttemptMapper {

    int insert(SysUserQuizAttempt attempt);

    SysUserQuizAttempt getById(@Param("id") Long id);

    /**
     * 查询用户当前进行中的答题（status=0）
     */
    SysUserQuizAttempt getInProgressByUserId(@Param("userId") Long userId);

    /**
     * 统计用户已提交的答题次数（status=1或2，不含进行中）
     */
    int countSubmittedByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的答题总数（含进行中）
     */
    int countAllByUserId(@Param("userId") Long userId);

    int update(SysUserQuizAttempt attempt);
}
