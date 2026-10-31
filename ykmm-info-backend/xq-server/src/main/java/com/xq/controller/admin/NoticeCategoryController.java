package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.NoticeCategoryDTO;
import com.xq.result.Result;
import com.xq.service.NoticeCategoryService;
import com.xq.vo.NoticeCategoryVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 公告分类管理
 */
@RestController("AdminNoticeCategoryController")
@RequestMapping("/admin/notice-category")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
public class NoticeCategoryController {

    @Autowired
    private NoticeCategoryService noticeCategoryService;

    @GetMapping("/list")
    public Result<List<NoticeCategoryVO>> list() {
        return Result.success(noticeCategoryService.listAll());
    }

    /**
     * 新增分类（id 需在 DTO 中手动指定，字典表不自动生成）
     */
    @PostMapping
    public Result<Void> create(@RequestBody @Validated NoticeCategoryDTO dto) {
        noticeCategoryService.create(dto);
        return Result.success();
    }

    /**
     * 编辑分类
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "分类ID不能为空") Integer id,
            @RequestBody @Validated NoticeCategoryDTO dto) {
        noticeCategoryService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除分类（分类下仍有公告时会拒绝）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable @NotNull(message = "分类ID不能为空") Integer id) {
        noticeCategoryService.delete(id);
        return Result.success();
    }
}
