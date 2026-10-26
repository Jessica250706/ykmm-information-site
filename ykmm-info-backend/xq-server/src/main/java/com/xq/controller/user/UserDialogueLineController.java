package com.xq.controller.user;

import com.xq.constant.DialogueConstant;
import com.xq.dto.DialogueLineDTO;
import com.xq.dto.DialogueLineSortDTO;
import com.xq.result.Result;
import com.xq.service.DialogueLineService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 对话句子
 * <p>
 * 权限：只能改自己创建的版本下的句子（管理员例外），
 * 由 Service 层校验 version.creatorId。
 */
@RestController("UserDialogueLineController")
@RequestMapping("/user/dialogue-line")
@Slf4j
@Validated
public class UserDialogueLineController {

    @Autowired
    private DialogueLineService dialogueLineService;

    /**
     * 批量保存句子
     */
    @PostMapping("/batch/{versionId}")
    public Result<Void> saveBatch(
            @PathVariable @NotNull(message = "版本ID不能为空") Long versionId,
            @RequestBody
            @Valid
            @NotEmpty(message = "句子列表不能为空")
            @Size(max = DialogueConstant.MAX_BATCH_SIZE,
                    message = "单次保存句子数量不能超过 " + DialogueConstant.MAX_BATCH_SIZE)
            List<DialogueLineDTO> lines
    ) {
        dialogueLineService.saveBatch(versionId, lines);
        return Result.success();
    }

    /**
     * 编辑单句
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "句子ID不能为空") Long id,
            @RequestBody
            @Valid
            @NotNull(message = "参数不能为空")
            DialogueLineDTO dto
    ) {
        dialogueLineService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除单句
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "句子ID不能为空") Long id) {
        dialogueLineService.delete(id);
        return Result.success();
    }

    /**
     * 批量更新
     */
    @PutMapping("/batch/{versionId}")
    public Result<Void> updateBatch(
            @PathVariable @NotNull(message = "版本ID不能为空") Long versionId,
            @RequestBody
            @Valid
            @NotEmpty(message = "句子列表不能为空")
            List<DialogueLineDTO> lines) {
        dialogueLineService.updateBatch(versionId, lines);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody @NotEmpty(message = "句子列表不能为空") List<Long> lineIds) {
        dialogueLineService.deleteBatch(lineIds);
        return Result.success();
    }

    /**
     * 调整句子顺序
     */
    @PutMapping("/sort/{versionId}")
    public Result<Void> sort(
            @PathVariable @NotNull(message = "版本ID不能为空") Long versionId,
            @RequestBody
            @Valid
            @NotNull(message = "参数不能为空")
            DialogueLineSortDTO dto) {
        dialogueLineService.sort(versionId, dto.getLineIds());
        return Result.success();
    }
}
