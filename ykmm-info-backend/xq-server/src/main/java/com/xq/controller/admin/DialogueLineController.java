package com.xq.controller.admin;

import com.xq.dto.DialogueLineDTO;
import com.xq.dto.DialogueLineSortDTO;
import com.xq.result.Result;
import com.xq.service.DialogueLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对话句子管理
 */
@RestController("AdminDialogueLineController")
@RequestMapping("/admin/dialogue-lines")
@Slf4j
public class DialogueLineController {

    @Autowired
    private DialogueLineService dialogueLineService;

    /**
     * 批量保存句子
     *
     * @param versionId 版本ID
     * @param lines     句子列表
     * @return 统一返回
     */
    @PostMapping("/batch/{versionId}")
    public Result<Void> saveBatch(@PathVariable Long versionId,
                                  @RequestBody List<DialogueLineDTO> lines) {
        dialogueLineService.saveBatch(versionId, lines);
        return Result.success();
    }

    /**
     * 编辑单句
     *
     * @param id  句子ID
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody DialogueLineDTO dto) {
        dialogueLineService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除单句
     *
     * @param id 句子ID
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dialogueLineService.delete(id);
        return Result.success();
    }

    /**
     * 调整句子顺序
     *
     * @param versionId 版本ID
     * @param dto       句子ID顺序
     * @return 统一返回
     */
    @PutMapping("/sort/{versionId}")
    public Result<Void> sort(@PathVariable Long versionId,
                             @RequestBody DialogueLineSortDTO dto) {
        dialogueLineService.sort(versionId, dto.getLineIds());
        return Result.success();
    }

    /**
     * 批量更新对话句子
     *
     * @param versionId 版本ID
     * @param lines     句子列表（每行带 id）
     * @return 统一返回
     */
    @PutMapping("/batch/{versionId}")
    public Result<Void> updateBatch(@PathVariable Long versionId,
                                    @RequestBody List<DialogueLineDTO> lines) {
        dialogueLineService.updateBatch(versionId, lines);
        return Result.success();
    }

    /**
     * 批量删除对话句子
     *
     * @param lineIds 句子ID列表
     * @return 统一返回
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> lineIds) {
        dialogueLineService.deleteBatch(lineIds);
        return Result.success();
    }
}
