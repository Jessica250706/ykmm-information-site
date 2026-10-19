package com.xq.controller.user;

import com.xq.dto.DialogueSourceQueryDTO;
import com.xq.dto.DialogueVersionDTO;
import com.xq.dto.DialogueVersionPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.DialogueVersionService;
import com.xq.vo.DialogueVersionVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 对话版本
 */
@RestController("UserDialogueVersionController")
@RequestMapping("/user/dialogue-version")
@Slf4j
@Validated
public class UserDialogueVersionController {

    @Autowired
    private DialogueVersionService dialogueVersionService;

    /**
     * 查询某来源下全部版本
     */
    @GetMapping("/list")
    public Result<List<DialogueVersionVO>> listBySource(@Validated DialogueSourceQueryDTO query) {
        return Result.success(dialogueVersionService.listBySource(query.getSourceType(), query.getSourceId()));
    }

    /**
     * 创建版本
     *
     * @param dto 创建参数
     * @return 新版本ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated DialogueVersionDTO dto) {
        return Result.success(dialogueVersionService.create(dto));
    }

    /**
     * 分页查询我上传的版本（含待审核 / 已发布 / 已拒绝）
     */
    @GetMapping("/my")
    public Result<PageResult<DialogueVersionVO>> pageMyVersions(
            @NotNull(message = "查询条件不能为空") DialogueVersionPageQueryDTO query) {
        return Result.success(dialogueVersionService.pageMyVersions(query));
    }
}
