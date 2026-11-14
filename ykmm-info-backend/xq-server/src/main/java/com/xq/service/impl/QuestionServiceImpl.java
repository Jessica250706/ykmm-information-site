package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.context.BaseContext;
import com.xq.dto.QuestionDTO;
import com.xq.dto.QuestionOptionDTO;
import com.xq.dto.QuestionPageQueryDTO;
import com.xq.entity.SysQuestion;
import com.xq.entity.SysQuestionOption;
import com.xq.enums.QuestionStatusEnum;
import com.xq.enums.QuestionTypeEnum;
import com.xq.exception.BusinessException;
import com.xq.mapper.QuestionMapper;
import com.xq.mapper.QuestionOptionMapper;
import com.xq.result.PageResult;
import com.xq.service.QuestionService;
import com.xq.vo.QuestionListVO;
import com.xq.vo.QuestionOptionVO;
import com.xq.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    @Override
    public PageResult<QuestionListVO> pageQuery(QuestionPageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<SysQuestion> list = questionMapper.pageQuery(query);
        PageInfo<SysQuestion> page = new PageInfo<>(list);

        List<QuestionListVO> voList = new ArrayList<>(page.getList().size());
        for (SysQuestion q : page.getList()) {
            QuestionListVO vo = new QuestionListVO();
            BeanUtils.copyProperties(q, vo);
            vo.setQuestionTypeLabel(QuestionTypeEnum.getLabel(q.getQuestionType()));
            vo.setStatusLabel(QuestionStatusEnum.getLabel(q.getStatus()));
            vo.setOptionCount(questionMapper.countOptionsByQuestionId(q.getId()));
            voList.add(vo);
        }
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public QuestionVO getById(Long id) {
        if (id == null) throw new BusinessException("题目ID不能为空");
        SysQuestion question = questionMapper.getById(id);
        if (question == null) throw new BusinessException("题目不存在");

        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        vo.setQuestionTypeLabel(QuestionTypeEnum.getLabel(question.getQuestionType()));
        vo.setStatusLabel(QuestionStatusEnum.getLabel(question.getStatus()));

        List<SysQuestionOption> options = questionOptionMapper.listByQuestionId(id);
        List<QuestionOptionVO> optionVOs = new ArrayList<>(options.size());
        for (SysQuestionOption o : options) {
            QuestionOptionVO oVO = new QuestionOptionVO();
            BeanUtils.copyProperties(o, oVO);
            optionVOs.add(oVO);
        }
        vo.setOptions(optionVOs);
        return vo;
    }

    @Override
    @Transactional
    public Long create(QuestionDTO dto) {
        validateQuestion(dto);

        SysQuestion question = new SysQuestion();
        BeanUtils.copyProperties(dto, question);
        question.setSort(dto.getSort() != null ? dto.getSort() : 0);
        question.setCreatorId(BaseContext.getCurrentId());
        questionMapper.insert(question);

        saveOptions(question.getId(), dto.getOptions());
        log.info("新增题目成功，id={}", question.getId());
        return question.getId();
    }

    @Override
    @Transactional
    public void update(Long id, QuestionDTO dto) {
        if (id == null) throw new BusinessException("题目ID不能为空");
        SysQuestion exist = questionMapper.getById(id);
        if (exist == null) throw new BusinessException("题目不存在");

        validateQuestion(dto);

        SysQuestion question = new SysQuestion();
        BeanUtils.copyProperties(dto, question);
        question.setId(id);
        questionMapper.update(question);

        // 选项全删全插（简化实现，避免复杂的增量比对）
        questionOptionMapper.deleteByQuestionId(id);
        saveOptions(id, dto.getOptions());

        log.info("编辑题目成功，id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BusinessException("题目ID不能为空");
        SysQuestion exist = questionMapper.getById(id);
        if (exist == null) throw new BusinessException("题目不存在");

        questionOptionMapper.deleteByQuestionId(id);
        questionMapper.deleteById(id);
        log.info("删除题目成功，id={}", id);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 校验题目和选项
     */
    private void validateQuestion(QuestionDTO dto) {
        if (!QuestionTypeEnum.isValid(dto.getQuestionType())) {
            throw new BusinessException("题型不合法");
        }
        if (!QuestionStatusEnum.isValid(dto.getStatus())) {
            throw new BusinessException("状态不合法");
        }
        if (dto.getOptions() == null || dto.getOptions().size() < 2) {
            throw new BusinessException("至少需要 2 个选项");
        }

        long correctCount = dto.getOptions().stream()
                .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                .count();

        if (correctCount == 0) {
            throw new BusinessException("必须至少设置一个正确答案");
        }
        if (QuestionTypeEnum.SINGLE.getValue().equals(dto.getQuestionType()) && correctCount > 1) {
            throw new BusinessException("单选题只能有一个正确答案");
        }
    }

    /**
     * 保存选项
     */
    private void saveOptions(Long questionId, List<QuestionOptionDTO> options) {
        List<SysQuestionOption> list = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            QuestionOptionDTO d = options.get(i);
            SysQuestionOption o = new SysQuestionOption();
            o.setQuestionId(questionId);
            o.setContent(d.getContent());
            o.setIsCorrect(d.getIsCorrect() != null ? d.getIsCorrect() : 0);
            o.setSort(d.getSort() != null ? d.getSort() : i + 1);
            list.add(o);
        }
        questionOptionMapper.insertBatch(list);
    }
}
