package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CardRtvService;
import com.xq.vo.CardRtvVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡面 RTV 管理
 */
@RestController("PublicCardRtvController")
@RequestMapping("/pub/card-rtv")
@Slf4j
public class PublicCardRtvController {

    @Autowired
    private CardRtvService cardRtvService;

    /**
     * 查询卡面下的 RTV 列表
     *
     * @param cardId 卡面ID
     * @return RTV 列表
     */
    @GetMapping("/card/{cardId}")
    public Result<List<CardRtvVO>> listByCardId(@PathVariable Long cardId) {
        return Result.success(cardRtvService.listByCardId(cardId));
    }
}
