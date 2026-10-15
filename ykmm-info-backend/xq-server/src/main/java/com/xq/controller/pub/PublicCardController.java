package com.xq.controller.pub;

import com.xq.dto.CardPageQueryDTO;
import com.xq.dto.DialogueSourceQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.CardService;
import com.xq.vo.CardEpisodeVO;
import com.xq.vo.CardVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 卡面浏览
 */
@RestController("PubCardController")
@RequestMapping("/pub/card")
@Slf4j
@Validated
public class PublicCardController {

    @Autowired
    private CardService cardService;

    /**
     * 分页查询卡面
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<CardVO>> page(@NotNull(message = "查询条件不能为空") CardPageQueryDTO query) {
        return Result.success(cardService.pageQuery(query));
    }

    /**
     * 查询卡面详情
     *
     * @param id 主键
     * @return 卡面详情
     */
    @GetMapping("/{id}")
    public Result<CardVO> detail(@PathVariable @NotNull(message = "卡面ID不能为空") Long id) {
        return Result.success(cardService.getById(id));
    }

    /**
     * 查询某一话的对话
     *
     * @param query 对话来源查询参数
     * @return 话详情（含对话版本）
     */
    @GetMapping("/episode/dialogue")
    public Result<CardEpisodeVO> episodeDialogue(@Valid DialogueSourceQueryDTO query) {
        return Result.success(cardService.getEpisodeDialogue(query.getSourceType(), query.getSourceId()));
    }

    /**
     * 判断当前用户能否新增 RC、RTV、Rabitter
     *
     * @param cardId 卡面ID
     * @return true 表示可以新增
     */
    @GetMapping("/can-create/{cardId}")
    public Result<Boolean> canCreate(@PathVariable Long cardId) {
        return Result.success(cardService.canCreate(cardId));
    }
}
