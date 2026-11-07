package com.xq.service;

import com.xq.dto.QuizSubmitDTO;
import com.xq.vo.QuizAttemptVO;
import com.xq.vo.QuizStatusVO;
import com.xq.vo.QuizSubmitResultVO;

/**
 * 用户答题服务
 */
public interface UserQuizService {

    /**
     * 开始答题：
     * - 已通过则拒绝
     * - 有进行中的则复用
     * - 检查次数限制
     * - 抽题 + 乱序 + 生成快照
     */
    QuizAttemptVO start();

    /**
     * 提交答案：
     * - 校验 attempt 归属和状态
     * - 逐题判分
     * - 更新 attempt 和 quiz_passed
     */
    QuizSubmitResultVO submit(QuizSubmitDTO dto);

    /**
     * 查询当前用户答题状态
     */
    QuizStatusVO getStatus();

    /**
     * 当前用户是否需要答题（供拦截器/前端调用）
     */
    boolean isRequired();
}
