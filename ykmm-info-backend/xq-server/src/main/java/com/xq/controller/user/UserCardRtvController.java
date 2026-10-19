package com.xq.controller.user;

import com.xq.dto.CardRtvDTO;
import com.xq.result.Result;
import com.xq.service.CardRtvService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 卡面 RTV 管理
 */
@RestController("UserCardRtvController")
@RequestMapping("/user/card-rtv")
@Slf4j
@Validated
public class UserCardRtvController {

    @Autowired
    private CardRtvService cardRtvService;

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated(CreateGroup.class) CardRtvDTO dto) {
        return Result.success(cardRtvService.create(dto));
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @Positive(message = "Rabitter ID不能为空") Long id,
            @RequestBody @Validated(UpdateGroup.class) CardRtvDTO dto
    ) {
        cardRtvService.update(id, dto);
        return Result.success();
    }
}
