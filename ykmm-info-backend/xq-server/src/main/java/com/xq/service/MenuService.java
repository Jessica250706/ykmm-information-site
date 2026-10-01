package com.xq.service;

import com.xq.dto.MenuDTO;
import com.xq.vo.MenuVO;

import java.util.List;

public interface MenuService {

    /** 查询菜单树 */
    List<MenuVO> tree(Integer menuType);

    /** 新增菜单 */
    void create(MenuDTO dto);

    /** 编辑菜单 */
    void update(Long id, MenuDTO dto);

    /** 删除菜单 */
    void delete(Long id);
}