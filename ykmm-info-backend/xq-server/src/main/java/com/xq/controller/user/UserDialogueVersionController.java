package com.xq.controller.user;

import com.xq.dto.DialogueSourceQueryDTO;
import com.xq.dto.DialogueVersionDTO;
import com.xq.result.Result;
import com.xq.service.DialogueVersionService;
import com.xq.vo.DialogueVersionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 对话版本
 * <p>
 * - 新增：贡献者信息 + creatorId 从 BaseContext 获取
 * - 删除：仅创建者可删（管理员例外）
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
}
