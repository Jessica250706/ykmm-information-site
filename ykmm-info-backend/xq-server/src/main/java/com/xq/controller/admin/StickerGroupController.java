package com.xq.controller.admin;

import com.github.pagehelper.PageInfo;
import com.xq.dto.StickerGroupDTO;
import com.xq.dto.StickerGroupPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StickerGroupService;
import com.xq.vo.StickerGroupVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表情包分组Controller
 */
@RestController("AdminStickerGroupController")
@RequestMapping("/admin/sticker-group")
@RequiredArgsConstructor
@Validated
public class StickerGroupController {

    private final StickerGroupService stickerGroupService;

    /**
     * 分页查询表情包分组
     */
    @GetMapping("/page")
    public Result<PageResult<StickerGroupVO>> page(StickerGroupPageQueryDTO queryDTO) {
        return Result.success(stickerGroupService.pageQuery(queryDTO));
    }

    /**
     * 查询所有表情包分组，用于下拉选择
     */
    @GetMapping("/list")
    public Result<List<StickerGroupVO>> list() {
        return Result.success(stickerGroupService.listAll());
    }

    /**
     * 根据ID查询表情包分组
     */
    @GetMapping("/{id}")
    public Result<StickerGroupVO> getById(@PathVariable Long id) {
        return Result.success(stickerGroupService.getById(id));
    }

    /**
     * 新增表情包分组
     */
    @PostMapping
    public Result<Void> save(@RequestBody @Valid StickerGroupDTO stickerGroupDTO) {
        stickerGroupService.save(stickerGroupDTO);
        return Result.success();
    }

    /**
     * 更新表情包分组
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid StickerGroupDTO stickerGroupDTO) {
        stickerGroupDTO.setId(id);
        stickerGroupService.update(stickerGroupDTO);
        return Result.success();
    }

    /**
     * 删除表情包分组
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stickerGroupService.delete(id);
        return Result.success();
    }
}
