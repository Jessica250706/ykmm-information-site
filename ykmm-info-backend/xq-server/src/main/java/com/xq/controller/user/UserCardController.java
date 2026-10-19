package com.xq.controller.user;

import com.xq.dto.CardDTO;
import com.xq.result.Result;
import com.xq.service.CardService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 卡面管理
 * <p>
 * 说明：
 * - 新增：status 由 Service 设为"待审核"，creatorId 取 BaseContext
 * - 更新：Service 层校验 creatorId 是否为当前用户（管理员例外）
 */
@RestController("UserCardController")
@RequestMapping("/user/card")
@Slf4j
@Validated
public class UserCardController {

    @Autowired
    private CardService cardService;

    /**
     * 新增卡面
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated(CreateGroup.class) CardDTO dto) {
        return Result.success(cardService.create(dto));
    }

    /**
     * 编辑卡面
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated(UpdateGroup.class) CardDTO dto) {
        cardService.update(id, dto);
        return Result.success();
    }
}
