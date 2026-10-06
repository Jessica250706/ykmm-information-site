package com.xq.controller.admin;

import com.xq.dto.CardRtvDTO;
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
@RestController("AdminCardRtvController")
@RequestMapping("/admin/card-rtv")
@Slf4j
public class CardRtvController {

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

    /**
     * 查询详情
     *
     * @param id 主键
     * @return RTV 详情
     */
    @GetMapping("/{id}")
    public Result<CardRtvVO> detail(@PathVariable Long id) {
        return Result.success(cardRtvService.getById(id));
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Long> create(@RequestBody CardRtvDTO dto) {
        return Result.success(cardRtvService.create(dto));
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CardRtvDTO dto) {
        cardRtvService.update(id, dto);
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
        cardRtvService.delete(id);
        return Result.success();
    }
}
