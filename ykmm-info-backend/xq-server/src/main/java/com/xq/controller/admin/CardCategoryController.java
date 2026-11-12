package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.CardCategoryDTO;
import com.xq.result.Result;
import com.xq.service.CardCategoryService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 卡面类别
 */
@RestController("AdminCardCategoryController")
@RequestMapping("/admin/card-category")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
public class CardCategoryController {

    @Autowired
    private CardCategoryService cardCategoryService;

    /**
     * 新增类别
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @PostMapping
    public Result<Integer> create(
            @RequestBody @Validated(CreateGroup.class) CardCategoryDTO dto) {
        return Result.success(cardCategoryService.create(dto));
    }

    /**
     * 编辑类别
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "类别ID不能为空") Integer id,
            @RequestBody @Validated(UpdateGroup.class) CardCategoryDTO dto) {
        cardCategoryService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除类别
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable @NotNull(message = "类别ID不能为空") Integer id) {
        cardCategoryService.delete(id);
        return Result.success();
    }
}
