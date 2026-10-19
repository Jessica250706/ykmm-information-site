package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
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

/**
 * 对话版本管理
 */
@RestController("AdminDialogueVersionController")
@RequestMapping("/admin/dialogue-version")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
public class DialogueVersionController {

    @Autowired
    private DialogueVersionService dialogueVersionService;

    /**
     * 分页查询对话版本
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<DialogueVersionVO>> page(@NotNull(message = "查询条件不能为空") DialogueVersionPageQueryDTO query) {
        return Result.success(dialogueVersionService.pageQuery(query));
    }

    /**
     * 删除版本
     *
     * @param id 版本ID
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "版本ID不能为空") Long id) {
        dialogueVersionService.delete(id);
        return Result.success();
    }
}
