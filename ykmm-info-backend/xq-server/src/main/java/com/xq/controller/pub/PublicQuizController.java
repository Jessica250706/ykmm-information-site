package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.UserQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共 - 答题状态（游客返回 false）
 */
@RestController("PubQuizController")
@RequestMapping("/pub/quiz")
@Slf4j
@Validated
@RequiredArgsConstructor
public class PublicQuizController {

    private final UserQuizService userQuizService;

    /**
     * 当前用户是否需要答题
     * <p>
     * 游客 / 已通过 / 答题关闭 → false
     * 登录用户且未通过答题 → true
     */
    @GetMapping("/required")
    public Result<Boolean> required() {
        return Result.success(userQuizService.isRequired());
    }
}
