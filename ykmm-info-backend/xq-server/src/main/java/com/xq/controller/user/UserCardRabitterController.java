package com.xq.controller.user;

import com.xq.dto.CardRabitterDTO;
import com.xq.result.Result;
import com.xq.service.CardRabitterService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 卡面 Rabitter 管理
 */
@RestController("UserCardRabitterController")
@RequestMapping("/user/card-rabitter")
@Slf4j
@Validated
public class UserCardRabitterController {

    @Autowired
    private CardRabitterService cardRabitterService;

    /**
     * 新增
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated(CreateGroup.class) CardRabitterDTO dto) {
        return Result.success(cardRabitterService.create(dto));
    }

    /**
     * 编辑
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @Positive(message = "Rabitter ID不能为空") Long id,
            @RequestBody @Validated(UpdateGroup.class) CardRabitterDTO dto
    ) {
        cardRabitterService.update(id, dto);
        return Result.success();
    }
}
