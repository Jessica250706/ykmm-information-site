package com.xq.controller.admin;

import com.xq.dto.DialogueVersionDTO;
import com.xq.dto.DialogueVersionPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.DialogueVersionService;
import com.xq.vo.DialogueVersionOptionVO;
import com.xq.vo.DialogueVersionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对话版本管理
 */
@RestController("AdminDialogueVersionController")
@RequestMapping("/admin/dialogue-versions")
@Slf4j
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
    public Result<PageResult<DialogueVersionVO>> page(DialogueVersionPageQueryDTO query) {
        return Result.success(dialogueVersionService.pageQuery(query));
    }

    /**
     * 查询某来源下的全部版本
     *
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     * @return 版本列表
     */
    @GetMapping("/list")
    public Result<List<DialogueVersionVO>> listBySource(
            @RequestParam Integer sourceType,
            @RequestParam Long sourceId) {
        return Result.success(
                dialogueVersionService.listBySource(sourceType, sourceId));
    }

    /**
     * 查询版本详情
     *
     * @param id 版本ID
     * @return 版本详情
     */
    @GetMapping("/{id}")
    public Result<DialogueVersionVO> detail(@PathVariable Long id) {
        return Result.success(dialogueVersionService.detail(id));
    }

    /**
     * 创建版本
     *
     * @param dto 创建参数
     * @return 新版本ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody DialogueVersionDTO dto) {
        return Result.success(dialogueVersionService.create(dto));
    }

    /**
     * 删除版本
     *
     * @param id 版本ID
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dialogueVersionService.delete(id);
        return Result.success();
    }

    /**
     * 查询全部文字版本选项（供下拉框使用）
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @return 选项列表
     */
    @GetMapping("/text-options")
    public Result<List<DialogueVersionOptionVO>> textOptions(
            @RequestParam Integer sourceType,
            @RequestParam Long sourceId) {
        return Result.success(dialogueVersionService.listTextVersionOptions(sourceType, sourceId));
    }
}
