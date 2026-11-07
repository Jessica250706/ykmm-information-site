package com.xq.controller.user;

import com.xq.dto.QuizSubmitDTO;
import com.xq.result.Result;
import com.xq.service.UserQuizService;
import com.xq.vo.QuizAttemptVO;
import com.xq.vo.QuizStatusVO;
import com.xq.vo.QuizSubmitResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 答题
 */
@RestController("UserQuizController")
@RequestMapping("/user/quiz")
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserQuizController {

    private final UserQuizService userQuizService;

    /**
     * 开始答题（若有进行中则复用）
     */
    @PostMapping("/start")
    public Result<QuizAttemptVO> start() {
        return Result.success(userQuizService.start());
    }

    /**
     * 提交答案
     */
    @PostMapping("/submit")
    public Result<QuizSubmitResultVO> submit(@RequestBody @Validated QuizSubmitDTO dto) {
        return Result.success(userQuizService.submit(dto));
    }

    /**
     * 查询当前用户的答题状态
     */
    @GetMapping("/status")
    public Result<QuizStatusVO> status() {
        return Result.success(userQuizService.getStatus());
    }
}
