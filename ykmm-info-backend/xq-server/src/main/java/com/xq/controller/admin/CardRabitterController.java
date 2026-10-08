package com.xq.controller.admin;

import com.xq.dto.CardRabitterDTO;
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
@RestController("AdminCardRabitterController")
@RequestMapping("/admin/card-rabitter")
@Slf4j
public class CardRabitterController {

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

    /**
     * 查询详情
     *
     * @param id 主键
     * @return Rabitter 详情
     */
    @GetMapping("/{id}")
    public Result<CardRabitterVO> detail(@PathVariable Long id) {
        return Result.success(cardRabitterService.getById(id));
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Long> create(@RequestBody CardRabitterDTO dto) {
        return Result.success(cardRabitterService.create(dto));
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CardRabitterDTO dto) {
        cardRabitterService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cardRabitterService.delete(id);
        return Result.success();
    }
}
