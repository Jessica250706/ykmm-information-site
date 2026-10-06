package com.xq.controller.admin;

import com.xq.dto.CardRcDTO;
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
@RestController("AdminCardRcController")
@RequestMapping("/admin/card-rc")
@Slf4j
public class CardRcController {

    @Autowired
    private CardRcService cardRcService;

    /** 查询卡面下的 RC 列表 */
    @GetMapping("/card/{cardId}")
    public Result<List<CardRcVO>> listByCardId(@PathVariable Long cardId) {
        return Result.success(cardRcService.listByCardId(cardId));
    }

    /** 查询详情 */
    @GetMapping("/{id}")
    public Result<CardRcVO> detail(@PathVariable Long id) {
        return Result.success(cardRcService.getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<Long> create(@RequestBody CardRcDTO dto) {
        return Result.success(cardRcService.create(dto));
    }

    /** 编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CardRcDTO dto) {
        cardRcService.update(id, dto);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cardRcService.delete(id);
        return Result.success();
    }
}
