package com.xq.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xq.constant.QuizAttemptStatusConstant;
import com.xq.constant.QuizPassedConstant;
import com.xq.context.BaseContext;
import com.xq.dto.QuizAnswerDTO;
import com.xq.dto.QuizSubmitDTO;
import com.xq.entity.*;
import com.xq.enums.QuizAttemptStatusEnum;
import com.xq.exception.*;
import com.xq.mapper.*;
import com.xq.service.UserQuizService;
import com.xq.vo.QuizAttemptVO;
import com.xq.vo.QuizQuestionVO;
import com.xq.vo.QuizStatusVO;
import com.xq.vo.QuizSubmitResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户答题服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserQuizServiceImpl implements UserQuizService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final QuizConfigMapper quizConfigMapper;
    private final UserQuizAttemptMapper attemptMapper;
    private final UserQuizQuestionMapper quizQuestionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public QuizAttemptVO start() {
        Long userId = requireUserId();

        SysQuizConfig config = quizConfigMapper.get();
        if (config == null || config.getEnabled() == null || config.getEnabled() == 0) {
            throw new BusinessException("答题未开启");
        }

        // 1. 已通过直接拒绝
        SysUser user = userMapper.getById(userId);
        if (user != null && QuizPassedConstant.PASSED == (user.getQuizPassed() == null ? 0 : user.getQuizPassed())) {
            throw new BusinessException("您已通过答题");
        }

        // 2. 有进行中的答题，直接复用
        SysUserQuizAttempt inProgress = attemptMapper.getInProgressByUserId(userId);
        if (inProgress != null) {
            return buildAttemptVO(inProgress);
        }

        // 3. 检查次数限制
        if (config.getMaxAttempts() != null && config.getMaxAttempts() > 0) {
            int used = attemptMapper.countSubmittedByUserId(userId);
            if (used >= config.getMaxAttempts()) {
                throw new BusinessException("答题次数已用完（" + config.getMaxAttempts() + " 次）");
            }
        }

        // 4. 抽题
        List<SysQuestion> fixed = questionMapper.listFixedEnabled();
        List<SysQuestion> randomPool = new ArrayList<>(questionMapper.listRandomEnabled());

        int totalCount = config.getTotalCount();
        int needRandom = totalCount - fixed.size();
        if (needRandom < 0) {
            throw new BusinessException("固定题数量（" + fixed.size() + "）超过配置的总题数（" + totalCount + "）");
        }
        if (randomPool.size() < needRandom) {
            throw new BusinessException("随机题不足，无法开始答题");
        }

        Collections.shuffle(randomPool);
        List<SysQuestion> picked = new ArrayList<>(totalCount);
        picked.addAll(fixed);
        picked.addAll(randomPool.subList(0, needRandom));
        Collections.shuffle(picked);

        // 5. 创建 attempt
        SysUserQuizAttempt attempt = new SysUserQuizAttempt();
        attempt.setUserId(userId);
        attempt.setStatus(QuizAttemptStatusConstant.IN_PROGRESS);
        attempt.setTotalCount(totalCount);
        attempt.setPassCount(config.getPassCount());
        attempt.setCorrectCount(0);
        attempt.setStartedAt(LocalDateTime.now());
        attemptMapper.insert(attempt);

        // 6. 生成快照
        List<SysUserQuizQuestion> snapshots = new ArrayList<>(picked.size());
        for (int i = 0; i < picked.size(); i++) {
            SysQuestion q = picked.get(i);
            List<SysQuestionOption> options = questionOptionMapper.listByQuestionId(q.getId());

            List<Long> order = options.stream()
                    .map(SysQuestionOption::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(order);

            // 快照只存必要字段
            List<Map<String, Object>> optionSnapshots = new ArrayList<>(options.size());
            for (SysQuestionOption o : options) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", o.getId());
                m.put("content", o.getContent());
                m.put("correct", o.getIsCorrect() != null && o.getIsCorrect() == 1);
                optionSnapshots.add(m);
            }

            SysUserQuizQuestion snap = new SysUserQuizQuestion();
            snap.setAttemptId(attempt.getId());
            snap.setQuestionId(q.getId());
            snap.setQuestionContent(q.getContent());
            snap.setQuestionType(q.getQuestionType());
            snap.setIsFixed(q.getIsFixed());
            snap.setOptionsSnapshot(writeJson(optionSnapshots));
            snap.setOptionOrder(writeJson(order));
            snap.setSort(i);
            snapshots.add(snap);
        }
        quizQuestionMapper.insertBatch(snapshots);

        log.info("用户开始答题，userId={}, attemptId={}, totalCount={}", userId, attempt.getId(), totalCount);
        return buildAttemptVO(attempt);
    }

    @Override
    @Transactional
    public QuizSubmitResultVO submit(QuizSubmitDTO dto) {
        Long userId = requireUserId();

        SysUserQuizAttempt attempt = attemptMapper.getById(dto.getAttemptId());
        if (attempt == null) throw new BusinessException("答题记录不存在");
        if (!Objects.equals(attempt.getUserId(), userId)) throw new ForbiddenException("无权提交此答题");
        if (QuizAttemptStatusConstant.IN_PROGRESS != attempt.getStatus()) {
            throw new AlreadyExistsException("该答题已提交");
        }

        Map<Long, QuizAnswerDTO> answerMap = dto.getAnswers().stream()
                .collect(Collectors.toMap(QuizAnswerDTO::getQuestionId, a -> a, (a, b) -> a));

        List<SysUserQuizQuestion> snapshots = quizQuestionMapper.listByAttemptId(attempt.getId());

        int correctCount = 0;
        List<QuizSubmitResultVO.QuestionResult> results = new ArrayList<>(snapshots.size());

        for (SysUserQuizQuestion snap : snapshots) {
            List<Map<String, Object>> options = readJson(snap.getOptionsSnapshot(),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Set<Long> correctIds = options.stream()
                    .filter(m -> Boolean.TRUE.equals(m.get("correct")))
                    .map(m -> ((Number) m.get("id")).longValue())
                    .collect(Collectors.toSet());

            QuizAnswerDTO answer = answerMap.get(snap.getQuestionId());
            List<Long> userOptionIds = answer != null && answer.getOptionIds() != null
                    ? answer.getOptionIds() : Collections.emptyList();
            Set<Long> userSet = new HashSet<>(userOptionIds);

            boolean correct = !userOptionIds.isEmpty() && userSet.equals(correctIds);
            if (correct) correctCount++;

            snap.setUserAnswer(userOptionIds.isEmpty() ? null : writeJson(userOptionIds));
            snap.setIsCorrect(correct ? 1 : 0);
            quizQuestionMapper.update(snap);

            QuizSubmitResultVO.QuestionResult r = new QuizSubmitResultVO.QuestionResult();
            r.setQuestionId(snap.getQuestionId());
            r.setContent(snap.getQuestionContent());
            r.setIsCorrect(correct);
            r.setUserAnswer(userOptionIds);
            r.setCorrectAnswer(new ArrayList<>(correctIds));
            results.add(r);
        }

        // 更新 attempt
        attempt.setCorrectCount(correctCount);
        attempt.setSubmittedAt(LocalDateTime.now());
        boolean passed = correctCount >= attempt.getPassCount();
        attempt.setStatus(passed
                ? QuizAttemptStatusConstant.PASSED
                : QuizAttemptStatusConstant.FAILED);
        attemptMapper.update(attempt);

        // 通过则更新用户
        if (passed) {
            userMapper.updateQuizPassed(userId, QuizPassedConstant.PASSED);
        }

        QuizSubmitResultVO result = new QuizSubmitResultVO();
        result.setAttemptId(attempt.getId());
        result.setStatus(attempt.getStatus());
        result.setStatusLabel(QuizAttemptStatusEnum.getLabel(attempt.getStatus()));
        result.setCorrectCount(correctCount);
        result.setTotalCount(attempt.getTotalCount());
        result.setPassCount(attempt.getPassCount());
        result.setPassed(passed);
        result.setQuestions(results);

        log.info("用户提交答题，userId={}, attemptId={}, correct={}/{}, passed={}",
                userId, attempt.getId(), correctCount, attempt.getTotalCount(), passed);
        return result;
    }

    @Override
    public QuizStatusVO getStatus() {
        Long userId = requireUserId();
        SysQuizConfig config = quizConfigMapper.get();
        SysUser user = userMapper.getById(userId);
        SysUserQuizAttempt inProgress = attemptMapper.getInProgressByUserId(userId);

        QuizStatusVO vo = new QuizStatusVO();
        vo.setQuizPassed(user != null && QuizPassedConstant.PASSED ==
                (user.getQuizPassed() == null ? 0 : user.getQuizPassed()));
        vo.setEnabled(config != null && config.getEnabled() != null && config.getEnabled() == 1);
        vo.setHasInProgress(inProgress != null);
        vo.setInProgressAttemptId(inProgress != null ? inProgress.getId() : null);
        vo.setMaxAttempts(config != null && config.getMaxAttempts() != null ? config.getMaxAttempts() : 0);

        int used = attemptMapper.countSubmittedByUserId(userId);
        vo.setAttemptsUsed(used);

        boolean canAttempt = vo.getEnabled()
                && !vo.getQuizPassed()
                && (vo.getMaxAttempts() == 0 || used < vo.getMaxAttempts())
                && !vo.getHasInProgress();
        vo.setCanAttempt(canAttempt);
        return vo;
    }

    @Override
    public boolean isRequired() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) return false;

        SysQuizConfig config = quizConfigMapper.get();
        if (config == null || config.getEnabled() == null || config.getEnabled() == 0) {
            return false;
        }
        SysUser user = userMapper.getById(userId);
        return user != null && QuizPassedConstant.NOT_PASSED ==
                (user.getQuizPassed() == null ? 0 : user.getQuizPassed());
    }

    /* ---------------- 私有方法 ---------------- */

    private Long requireUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) throw new UnauthorizedException("请先登录");
        return userId;
    }

    /**
     * 构建答题会话 VO：从快照读取题目、按 option_order 排序选项
     */
    private QuizAttemptVO buildAttemptVO(SysUserQuizAttempt attempt) {
        QuizAttemptVO vo = new QuizAttemptVO();
        vo.setAttemptId(attempt.getId());
        vo.setTotalCount(attempt.getTotalCount());
        vo.setPassCount(attempt.getPassCount());
        vo.setStartedAt(attempt.getStartedAt());

        List<SysUserQuizQuestion> snapshots = quizQuestionMapper.listByAttemptId(attempt.getId());
        List<QuizQuestionVO> questions = new ArrayList<>(snapshots.size());

        for (SysUserQuizQuestion snap : snapshots) {
            List<Map<String, Object>> options = readJson(snap.getOptionsSnapshot(),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            List<Long> order = readJson(snap.getOptionOrder(),
                    new TypeReference<List<Long>>() {
                    });

            // 建索引方便按 option_order 排序
            Map<Long, Map<String, Object>> optMap = options.stream()
                    .collect(Collectors.toMap(m -> ((Number) m.get("id")).longValue(), m -> m));

            List<QuizQuestionVO.OptionItem> items = new ArrayList<>();
            for (Long optId : order) {
                Map<String, Object> o = optMap.get(optId);
                if (o == null) continue;
                QuizQuestionVO.OptionItem item = new QuizQuestionVO.OptionItem();
                item.setId(optId);
                item.setContent((String) o.get("content"));
                items.add(item);
            }

            QuizQuestionVO q = new QuizQuestionVO();
            q.setQuestionId(snap.getQuestionId());
            q.setContent(snap.getQuestionContent());
            q.setQuestionType(snap.getQuestionType());
            q.setIsFixed(snap.getIsFixed());
            q.setSort(snap.getSort());
            q.setOptions(items);
            questions.add(q);
        }
        vo.setQuestions(questions);
        return vo;
    }

    private String writeJson(Object obj) {
        try {
            return JSON.writeValueAsString(obj);
        } catch (Exception e) {
            throw new SerializationException("JSON 序列化失败", e);
        }
    }

    private <T> T readJson(String json, TypeReference<T> typeRef) {
        try {
            return JSON.readValue(json, typeRef);
        } catch (Exception e) {
            throw new SerializationException("JSON 反序列化失败", e);
        }
    }
}
