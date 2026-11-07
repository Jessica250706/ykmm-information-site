package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.QuestionDTO;
import com.xq.dto.QuestionPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.QuestionService;
import com.xq.vo.QuestionListVO;
import com.xq.vo.QuestionVO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 题目管理
 */
@RestController("AdminQuestionController")
@RequestMapping("/admin/quiz/question")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 分页查询题目
     */
    @GetMapping("/page")
    public Result<PageResult<QuestionListVO>> page(
            @NotNull(message = "查询条件不能为空") QuestionPageQueryDTO query) {
        return Result.success(questionService.pageQuery(query));
    }

    /**
     * 题目详情（含选项和答案）
     */
    @GetMapping("/{id}")
    public Result<QuestionVO> detail(
            @PathVariable @NotNull(message = "题目ID不能为空") Long id) {
        return Result.success(questionService.getById(id));
    }

    /**
     * 新增题目
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated QuestionDTO dto) {
        return Result.success(questionService.create(dto));
    }

    /**
     * 编辑题目
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "题目ID不能为空") Long id,
            @RequestBody @Validated QuestionDTO dto) {
        questionService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable @NotNull(message = "题目ID不能为空") Long id) {
        questionService.delete(id);
        return Result.success();
    }
}
