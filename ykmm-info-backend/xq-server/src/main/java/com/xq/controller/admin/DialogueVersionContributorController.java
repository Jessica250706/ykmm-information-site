package com.xq.controller.admin;

import com.xq.dto.DialogueVersionContributorDTO;
import com.xq.dto.DialogueVersionContributorPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.DialogueVersionContributorService;
import com.xq.vo.DialogueVersionContributorVO;
import com.xq.vo.StoryContributorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对话版本贡献者管理
 */
@RestController("AdminDialogueVersionContributorController")
@RequestMapping("/admin/dialogue-version-contributor")
@Slf4j
public class DialogueVersionContributorController {

    @Autowired
    private DialogueVersionContributorService dialogueVersionContributorService;

    /**
     * 分页查询贡献者
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<DialogueVersionContributorVO>> page(DialogueVersionContributorPageQueryDTO query) {
        log.info("管理端分页查询贡献者：{}", query);
        return Result.success(dialogueVersionContributorService.pageQuery(query));
    }

    /**
     * 根据版本ID查询贡献者列表
     *
     * @param versionId 版本ID
     * @return 贡献者列表
     */
    @GetMapping("/version/{versionId}")
    public Result<List<DialogueVersionContributorVO>> listByVersionId(@PathVariable Long versionId) {
        return Result.success(dialogueVersionContributorService.listByVersionId(versionId));
    }

    /**
     * 根据来源聚合查询贡献者
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @return 贡献者列表
     */
    @GetMapping("/source/{sourceType}/{sourceId}")
    public Result<List<StoryContributorVO>> listBySource(@PathVariable Integer sourceType,
                                                         @PathVariable Long sourceId) {
        return Result.success(dialogueVersionContributorService.listContributorsBySource(sourceType, sourceId));
    }

    /**
     * 查询贡献者详情
     *
     * @param id 主键
     * @return 贡献者详情
     */
    @GetMapping("/{id}")
    public Result<DialogueVersionContributorVO> detail(@PathVariable Long id) {
        return Result.success(dialogueVersionContributorService.getById(id));
    }

    /**
     * 新增贡献者
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Long> create(@RequestBody DialogueVersionContributorDTO dto) {
        return Result.success(dialogueVersionContributorService.create(dto));
    }

    /**
     * 更新贡献者
     *
     * @param id  主键
     * @param dto 更新参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody DialogueVersionContributorDTO dto) {
        dialogueVersionContributorService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除贡献者
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dialogueVersionContributorService.delete(id);
        return Result.success();
    }

    /**
     * 根据版本ID清空贡献者
     *
     * @param versionId 版本ID
     * @return 统一返回
     */
    @DeleteMapping("/version/{versionId}")
    public Result<Void> deleteByVersionId(@PathVariable Long versionId) {
        dialogueVersionContributorService.deleteByVersionId(versionId);
        return Result.success();
    }
}
