package com.xq.controller.pub;

import com.xq.dto.CardRcDTO;
import com.xq.result.Result;
import com.xq.service.CardRcService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import com.xq.vo.CardRcVO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共 - 卡面 RC 管理
 */
@RestController("PublicCardRcController")
@RequestMapping("/pub/card-rc")
@Slf4j
@Validated
public class PublicCardRcController {

    @Autowired
    private CardRcService cardRcService;

    /** 查询卡面下的 RC 列表 */
    @GetMapping("/card/{cardId}")
    public Result<List<CardRcVO>> listByCardId(@PathVariable @NotNull(message = "卡面ID不能为空") Long cardId) {
        return Result.success(cardRcService.listByCardId(cardId));
    }

    /** 查询详情 */
    @GetMapping("/{id}")
    public Result<CardRcVO> detail(@PathVariable @NotNull(message = "RC ID不能为空") Long id) {
        return Result.success(cardRcService.getById(id));
    }

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
