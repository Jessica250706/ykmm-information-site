package com.xq.controller.admin;

import com.xq.dto.CardReviewDTO;
import com.xq.result.Result;
import com.xq.service.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面管理
 */
@RestController("AdminCardController")
@RequestMapping("/admin/card")
@Slf4j
@Validated
public class CardController {

    @Autowired
    private CardService cardService;

    /**
     * 删除卡面
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "卡面ID不能为空") Long id) {
        cardService.delete(id);
        return Result.success();
    }

    /**
     * 审核卡面
     *
     * @param id  卡面ID
     * @param dto 审核参数
     * @return 统一返回
     */
    @PostMapping("/{id}/review")
    public Result<Void> review(
            @PathVariable @NotNull(message = "卡面ID不能为空") Long id,
            @RequestBody @Valid CardReviewDTO dto
    ) {
        cardService.review(id, dto);
        return Result.success();
    }
}
