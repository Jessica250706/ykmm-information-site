package com.xq.service.impl;

import com.xq.dto.QuizConfigDTO;
import com.xq.entity.SysQuizConfig;
import com.xq.mapper.QuestionMapper;
import com.xq.mapper.QuizConfigMapper;
import com.xq.service.QuizConfigService;
import com.xq.vo.QuizConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 答题配置服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuizConfigServiceImpl implements QuizConfigService {

    private final QuizConfigMapper quizConfigMapper;
    private final QuestionMapper questionMapper;

    @Override
    public QuizConfigVO get() {
        SysQuizConfig config = quizConfigMapper.get();
        if (config == null) throw new RuntimeException("答题配置不存在");

        QuizConfigVO vo = new QuizConfigVO();
        BeanUtils.copyProperties(config, vo);
        vo.setFixedCount(questionMapper.countFixedEnabled());
        vo.setRandomCount(questionMapper.countRandomEnabled());
        return vo;
    }

    @Override
    @Transactional
    public void update(QuizConfigDTO dto) {
        if (dto.getPassCount() > dto.getTotalCount()) {
            throw new RuntimeException("通过题数不能大于总题数");
        }

        // 校验总题数能否被当前题库支持
        int fixedCount = questionMapper.countFixedEnabled();
        int randomCount = questionMapper.countRandomEnabled();

        if (dto.getTotalCount() < fixedCount) {
            throw new RuntimeException("总题数不能小于固定题数量（当前固定题 " + fixedCount + " 道）");
        }
        int needRandom = dto.getTotalCount() - fixedCount;
        if (needRandom > randomCount) {
            throw new RuntimeException("随机题不够：需要 " + needRandom + " 道，当前只有 " + randomCount + " 道");
        }

        SysQuizConfig config = new SysQuizConfig();
        BeanUtils.copyProperties(dto, config);
        quizConfigMapper.update(config);
        log.info("更新答题配置成功：{}", dto);
    }
}
