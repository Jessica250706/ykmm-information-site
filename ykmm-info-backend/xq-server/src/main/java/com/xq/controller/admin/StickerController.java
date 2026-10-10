package com.xq.controller.admin;

import com.github.pagehelper.PageInfo;
import com.xq.dto.StickerDTO;
import com.xq.dto.StickerPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StickerService;
import com.xq.vo.StickerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 表情包Controller
 */
@RestController("AdminStickerController")
@RequestMapping("/admin/sticker")
@RequiredArgsConstructor
@Validated
public class StickerController {

    private final StickerService stickerService;

    /**
     * 分页查询表情包
     */
    @GetMapping("/page")
    public Result<PageResult<StickerVO>> page(StickerPageQueryDTO queryDTO) {
        return Result.success(stickerService.pageQuery(queryDTO));
    }

    /**
     * 根据ID查询表情包
     */
    @GetMapping("/{id}")
    public Result<StickerVO> getById(@PathVariable Long id) {
        return Result.success(stickerService.getById(id));
    }

    /**
     * 新增表情包
     */
    @PostMapping
    public Result<Void> save(@RequestBody @Valid StickerDTO stickerDTO) {
        stickerService.save(stickerDTO);
        return Result.success();
    }

    /**
     * 更新表情包
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid StickerDTO stickerDTO) {
        stickerDTO.setId(id);
        stickerService.update(stickerDTO);
        return Result.success();
    }

    /**
     * 删除表情包
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stickerService.delete(id);
        return Result.success();
    }
}
