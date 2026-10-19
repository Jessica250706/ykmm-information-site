package com.xq.controller.user;

import com.xq.result.Result;
import com.xq.service.StickerGroupService;
import com.xq.vo.StickerGroupVO;
import com.xq.vo.StickerGroupWithStickersVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 表情包分组Controller
 */
@RestController("UserStickerGroupController")
@RequestMapping("/user/sticker-group")
@RequiredArgsConstructor
@Validated
public class UserStickerGroupController {

    private final StickerGroupService stickerGroupService;

    /**
     * 查询所有表情包分组，用于下拉选择
     */
    @GetMapping("/list")
    public Result<List<StickerGroupVO>> list() {
        return Result.success(stickerGroupService.listAll());
    }

    /**
     * 查询所有分组及下属表情包（表情选择器用）
     */
    @GetMapping("/with-stickers")
    public Result<List<StickerGroupWithStickersVO>> listWithStickers() {
        return Result.success(stickerGroupService.listWithStickers());
    }
}
