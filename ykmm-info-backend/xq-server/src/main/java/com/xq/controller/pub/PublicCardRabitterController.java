package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CardRabitterService;
import com.xq.vo.CardRabitterVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共 - 卡面 Rabitter 管理
 */
@RestController("PublicCardRabitterController")
@RequestMapping("/pub/card-rabitter")
@Slf4j
@Validated
public class PublicCardRabitterController {

    @Autowired
    private CardRabitterService cardRabitterService;

    /**
     * 查询卡面下的 Rabitter 列表
     *
     * @param cardId 卡面ID
     * @return Rabitter 列表
     */
    @GetMapping("/card/{cardId}")
    public Result<List<CardRabitterVO>> listByCardId(@PathVariable @NotNull(message = "卡面ID不能为空") Long cardId) {
        return Result.success(cardRabitterService.listByCardId(cardId));
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return Rabitter 详情
     */
    @GetMapping("/{id}")
    public Result<CardRabitterVO> detail(@PathVariable @NotNull(message = "Rabitter ID不能为空") Long id) {
        return Result.success(cardRabitterService.getById(id));
    }
}
