package com.xq.service;

import com.xq.dto.QuizConfigDTO;
import com.xq.vo.QuizConfigVO;

/**
 * 答题配置服务
 */
public interface QuizConfigService {

    /**
     * 获取配置（管理端，含题目统计）
     */
    QuizConfigVO get();

    /**
     * 修改配置（管理端）
     */
    void update(QuizConfigDTO dto);
}
