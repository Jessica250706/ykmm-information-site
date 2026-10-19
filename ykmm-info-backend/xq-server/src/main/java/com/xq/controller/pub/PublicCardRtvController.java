package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CardRtvService;
import com.xq.vo.CardRtvVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共 - 卡面 RTV 管理
 */
@RestController("PublicCardRtvController")
@RequestMapping("/pub/card-rtv")
@Slf4j
@Validated
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
    public Result<List<CardRtvVO>> listByCardId(@PathVariable @NotNull(message = "卡面ID不能为空") Long cardId) {
        return Result.success(cardRtvService.listByCardId(cardId));
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return RTV 详情
     */
    @GetMapping("/{id}")
    public Result<CardRtvVO> detail(@PathVariable @NotNull(message = "RTV ID不能为空")  Long id) {
        return Result.success(cardRtvService.getById(id));
    }
}
