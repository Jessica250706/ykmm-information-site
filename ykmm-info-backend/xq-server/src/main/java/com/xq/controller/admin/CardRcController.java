package com.xq.controller.admin;

import com.xq.result.Result;
import com.xq.service.CardRcService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面 RC 管理
 */
@RestController("AdminCardRcController")
@RequestMapping("/admin/card-rc")
@Slf4j
public class CardRcController {

    @Autowired
    private CardRcService cardRcService;

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "RC ID不能为空") Long id) {
        cardRcService.delete(id);
        return Result.success();
    }
}
