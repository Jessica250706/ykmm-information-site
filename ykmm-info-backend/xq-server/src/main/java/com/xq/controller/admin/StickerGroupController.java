package com.xq.controller.admin;

import com.xq.dto.StickerGroupDTO;
import com.xq.dto.StickerGroupPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StickerGroupService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import com.xq.vo.StickerGroupVO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * 根据ID查询表情包分组
     */
    @GetMapping("/{id}")
    public Result<StickerGroupVO> getById(@PathVariable @NotNull(message = "表情包分组ID不能为空") Long id) {
        return Result.success(stickerGroupService.getById(id));
    }

    /**
     * 新增表情包分组
     */
    @PostMapping
    public Result<Void> save(@RequestBody @Validated(CreateGroup.class) StickerGroupDTO stickerGroupDTO) {
        stickerGroupService.save(stickerGroupDTO);
        return Result.success();
    }

    /**
     * 更新表情包分组
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "表情包分组ID不能为空") Long id,
            @RequestBody @Validated(UpdateGroup.class) StickerGroupDTO stickerGroupDTO
    ) {
        stickerGroupDTO.setId(id);
        stickerGroupService.update(stickerGroupDTO);
        return Result.success();
    }

    /**
     * 删除表情包分组
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "表情包分组ID不能为空") Long id) {
        stickerGroupService.delete(id);
        return Result.success();
    }
}
