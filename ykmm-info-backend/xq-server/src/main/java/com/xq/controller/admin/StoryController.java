package com.xq.controller.admin;

import com.xq.dto.StoryAuditDTO;
import com.xq.dto.StoryDTO;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StoryService;
import com.xq.vo.StoryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 删除剧情
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "剧情ID不能为空") Long id) {
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
    public Result<Void> audit(@PathVariable @NotNull(message = "剧情ID不能为空") Long id,
                              @RequestBody @Valid StoryAuditDTO dto) {
        storyService.audit(id, dto);
        return Result.success();
    }

    /**
     * 根据分类ID查询剧情列表
     *
     * @param categoryId 分类ID
     * @return 剧情列表
     */
    @GetMapping("/by-category/{categoryId}")
    public Result<List<StoryVO>> listByCategoryId(@PathVariable @NotNull(message = "分类ID不能为空") Long categoryId) {
        return Result.success(storyService.listByCategoryId(categoryId));
    }
}
