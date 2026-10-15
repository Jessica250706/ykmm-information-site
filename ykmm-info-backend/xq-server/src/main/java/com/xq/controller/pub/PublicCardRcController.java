package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CardRcService;
import com.xq.vo.CardRcVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡面 RC 管理
 */
@RestController("PublicCardRcController")
@RequestMapping("/pub/card-rc")
@Slf4j
public class PublicCardRcController {

    @Autowired
    private CardRcService cardRcService;

    /** 查询卡面下的 RC 列表 */
    @GetMapping("/card/{cardId}")
    public Result<List<CardRcVO>> listByCardId(@PathVariable Long cardId) {
        return Result.success(cardRcService.listByCardId(cardId));
    }
}
