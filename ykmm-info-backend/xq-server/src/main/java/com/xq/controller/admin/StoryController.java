package com.xq.controller.admin;

import com.xq.dto.StoryAuditDTO;
import com.xq.dto.StoryDTO;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StoryService;
import com.xq.vo.StoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 剧情管理
 */
@RestController("AdminStoryController")
@RequestMapping("/admin/story")
@Slf4j
public class StoryController {

    @Autowired
    private StoryService storyService;

    /**
     * 分页查询剧情
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<StoryVO>> page(StoryPageQueryDTO query) {
        log.info("管理端查询剧情列表：{}", query);
        return Result.success(storyService.pageQuery(query));
    }

    /**
     * 查询剧情详情
     *
     * @param id 主键
     * @return 剧情详情
     */
    @GetMapping("/{id}")
    public Result<StoryVO> detail(@PathVariable Long id) {
        return Result.success(storyService.getById(id));
    }

    /**
     * 新增剧情
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @PostMapping
    public Result<Long> create(@RequestBody StoryDTO dto) {
        return Result.success(storyService.create(dto));
    }

    /**
     * 编辑剧情
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody StoryDTO dto) {
        storyService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除剧情
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        storyService.delete(id);
        return Result.success();
    }

    /**
     * 审核剧情
     *
     * @param id  主键
     * @param dto 审核参数
     * @return 统一返回
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestBody StoryAuditDTO dto) {
        storyService.audit(id, dto);
        return Result.success();
    }
}
