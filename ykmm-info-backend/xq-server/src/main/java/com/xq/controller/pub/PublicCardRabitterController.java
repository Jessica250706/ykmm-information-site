package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CardRabitterService;
import com.xq.vo.CardRabitterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡面 Rabitter 管理
 */
@RestController("PublicCardRabitterController")
@RequestMapping("/pub/card-rabitter")
@Slf4j
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
    public Result<List<CardRabitterVO>> listByCardId(@PathVariable Long cardId) {
        return Result.success(cardRabitterService.listByCardId(cardId));
    }
}
