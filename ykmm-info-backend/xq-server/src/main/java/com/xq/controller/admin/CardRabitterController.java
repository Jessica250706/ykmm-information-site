package com.xq.controller.admin;

import com.xq.result.Result;
import com.xq.service.CardRabitterService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面 Rabitter 管理
 */
@RestController("AdminCardRabitterController")
@RequestMapping("/admin/card-rabitter")
@Slf4j
@Validated
public class CardRabitterController {

    @Autowired
    private CardRabitterService cardRabitterService;

    /**
     * 删除
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "Rabitter ID不能为空") Long id) {
        cardRabitterService.delete(id);
        return Result.success();
    }
}
