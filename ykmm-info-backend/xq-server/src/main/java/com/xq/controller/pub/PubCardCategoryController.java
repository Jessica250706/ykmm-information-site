package com.xq.controller.pub;

import com.xq.dto.CardCategoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.CardCategoryService;
import com.xq.vo.CardCategoryVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共 - 卡面类别
 */
@RestController("PubCardCategoryController")
@RequestMapping("/pub/card-category")
@Slf4j
@Validated
public class PubCardCategoryController {

    @Autowired
    private CardCategoryService cardCategoryService;

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<CardCategoryVO>> page(CardCategoryPageQueryDTO query) {
        return Result.success(cardCategoryService.pageQuery(query));
    }

    /**
     * 查询全部类别（下拉用）
     */
    @GetMapping("/list")
    public Result<List<CardCategoryVO>> listAll() {
        return Result.success(cardCategoryService.listAll());
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 类别详情
     */
    @GetMapping("/{id}")
    public Result<CardCategoryVO> detail(
            @PathVariable @NotNull(message = "类别ID不能为空") Integer id) {
        return Result.success(cardCategoryService.getById(id));
    }
}
