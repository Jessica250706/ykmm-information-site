package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.CardSeriesDTO;
import com.xq.dto.CardSeriesPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.CardSeriesService;
import com.xq.vo.CardSeriesVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面系列管理
 */
@RestController("AdminCardSeriesController")
@RequestMapping("/admin/card-series")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
public class CardSeriesController {

    @Autowired
    private CardSeriesService cardSeriesService;

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<CardSeriesVO>> page(CardSeriesPageQueryDTO query) {
        return Result.success(cardSeriesService.pageQuery(query));
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 系列详情
     */
    @GetMapping("/{id}")
    public Result<CardSeriesVO> detail(@PathVariable Long id) {
        return Result.success(cardSeriesService.getById(id));
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Long> create(@RequestBody CardSeriesDTO dto) {
        return Result.success(cardSeriesService.create(dto));
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CardSeriesDTO dto) {
        cardSeriesService.update(id, dto);
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
        cardSeriesService.delete(id);
        return Result.success();
    }
}
