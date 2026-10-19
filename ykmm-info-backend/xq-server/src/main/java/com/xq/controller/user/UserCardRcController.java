package com.xq.controller.user;

import com.xq.dto.CardRcDTO;
import com.xq.result.Result;
import com.xq.service.CardRcService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 卡面 RC 管理
 */
@RestController("UserCardRcController")
@RequestMapping("/user/card-rc")
@Slf4j
@Validated
public class UserCardRcController {

    @Autowired
    private CardRcService cardRcService;

    /** 新增 */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated(CreateGroup.class) CardRcDTO dto) {
        return Result.success(cardRcService.create(dto));
    }

    /** 编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @Positive(message = "RC ID不能为空") Long id,
            @RequestBody @Validated(UpdateGroup.class) CardRcDTO dto
    ) {
        cardRcService.update(id, dto);
        return Result.success();
    }
}
