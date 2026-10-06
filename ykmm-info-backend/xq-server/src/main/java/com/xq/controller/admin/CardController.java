package com.xq.controller.admin;

import com.xq.dto.CardDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.CardService;
import com.xq.vo.CardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面管理
 */
@RestController("AdminCardController")
@RequestMapping("/admin/card")
@Slf4j
public class CardController {

    @Autowired
    private CardService cardService;

    /** 分页查询 */
    @GetMapping
    public Result<PageResult<CardVO>> page(CardPageQueryDTO query) {
        return Result.success(cardService.pageQuery(query));
    }

    /** 查询详情 */
    @GetMapping("/{id}")
    public Result<CardVO> detail(@PathVariable Long id) {
        return Result.success(cardService.getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<Long> create(@RequestBody CardDTO dto) {
        return Result.success(cardService.create(dto));
    }

    /** 编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CardDTO dto) {
        cardService.update(id, dto);
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return Result.success();
    }
}
