package com.xq.controller.admin;

import com.xq.dto.MenuDTO;
import com.xq.result.Result;
import com.xq.service.MenuService;
import com.xq.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目录管理
 */
@RestController("AdminMenuController")
@RequestMapping("/admin/menus")
@Slf4j
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 菜单树
     * menuType 可选：1管理端 2用户端，不传返回全部
     */
    @GetMapping
    public Result<List<MenuVO>> tree(
            @RequestParam(required = false) Integer menuType) {
        return Result.success(menuService.tree(menuType));
    }

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