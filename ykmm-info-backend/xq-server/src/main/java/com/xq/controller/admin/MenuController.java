package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.MenuDTO;
import com.xq.result.Result;
import com.xq.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 目录管理
 */
@RestController("AdminMenuController")
@RequestMapping("/admin/menu")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 新增菜单
     */
    @PostMapping
    public Result<Void> create(@RequestBody MenuDTO dto) {
        menuService.create(dto);
        return Result.success();
    }

    /**
     * 编辑菜单
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody MenuDTO dto) {
        menuService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
