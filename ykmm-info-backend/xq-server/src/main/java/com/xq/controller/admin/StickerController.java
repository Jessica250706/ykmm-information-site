package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.StickerDTO;
import com.xq.dto.StickerPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StickerService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import com.xq.vo.StickerVO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 表情包Controller
 */
@RestController("AdminStickerController")
@RequestMapping("/admin/sticker")
@RequireRole(UserRoleConstant.ADMIN)
@RequiredArgsConstructor
@Validated
public class StickerController {

    private final StickerService stickerService;

    /**
     * 分页查询表情包
     */
    @GetMapping("/page")
    public Result<PageResult<StickerVO>> page(@Validated StickerPageQueryDTO queryDTO) {
        return Result.success(stickerService.pageQuery(queryDTO));
    }

    /**
     * 根据ID查询表情包
     */
    @GetMapping("/{id}")
    public Result<StickerVO> getById(@PathVariable @NotNull(message = "表情包ID不能为空") Long id) {
        return Result.success(stickerService.getById(id));
    }

    /**
     * 新增表情包
     */
    @PostMapping
    public Result<Void> save(@RequestBody @Validated(CreateGroup.class) StickerDTO stickerDTO) {
        stickerService.save(stickerDTO);
        return Result.success();
    }

    /**
     * 更新表情包
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "表情包ID不能为空") Long id,
            @RequestBody @Validated(UpdateGroup.class) StickerDTO stickerDTO
    ) {
        stickerDTO.setId(id);
        stickerService.update(stickerDTO);
        return Result.success();
    }

    /**
     * 删除表情包
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "表情包ID不能为空") Long id) {
        stickerService.delete(id);
        return Result.success();
    }
}
