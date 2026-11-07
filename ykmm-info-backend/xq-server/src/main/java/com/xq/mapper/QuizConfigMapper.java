package com.xq.mapper;

import com.xq.entity.SysQuizConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题配置 Mapper（单行）
 */
@Mapper
public interface QuizConfigMapper {

    SysQuizConfig get();

    int update(SysQuizConfig config);
}
