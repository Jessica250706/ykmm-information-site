package com.xq.controller.pub;

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
@RestController("PublicMenuController")
@RequestMapping("/pub/menu")
@Slf4j
public class PublicMenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 菜单树
     * menuType 可选：1管理端 2用户端，不传返回全部
     */
    @GetMapping
    public Result<List<MenuVO>> tree(@RequestParam(required = false) Integer menuType) {
        return Result.success(menuService.tree(menuType));
    }
}
