package com.xq.controller.admin;

import com.xq.result.Result;
import com.xq.service.CardService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 卡面浏览
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
}
