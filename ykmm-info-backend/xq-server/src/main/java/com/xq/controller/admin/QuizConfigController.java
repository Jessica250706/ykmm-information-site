package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.QuizConfigDTO;
import com.xq.result.Result;
import com.xq.service.QuizConfigService;
import com.xq.vo.QuizConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 答题配置
 */
@RestController("AdminQuizConfigController")
@RequestMapping("/admin/quiz/config")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
@RequiredArgsConstructor
public class QuizConfigController {

    private final QuizConfigService quizConfigService;

    /**
     * 获取配置（含题目统计）
     */
    @GetMapping
    public Result<QuizConfigVO> get() {
        return Result.success(quizConfigService.get());
    }

    /**
     * 修改配置
     */
    @PutMapping
    public Result<Void> update(@RequestBody @Validated QuizConfigDTO dto) {
        quizConfigService.update(dto);
        return Result.success();
    }
}
