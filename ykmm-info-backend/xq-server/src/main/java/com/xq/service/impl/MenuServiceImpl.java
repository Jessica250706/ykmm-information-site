package com.xq.service.impl;

import com.xq.dto.MenuDTO;
import com.xq.entity.SysMenu;
import com.xq.mapper.MenuMapper;
import com.xq.service.MenuService;
import com.xq.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 查询菜单树
     * menuType 为 null 时返回全部
     */
    @Override
    public List<MenuVO> tree(Integer menuType) {
        List<MenuVO> all;
        if (menuType == null) {
            all = menuMapper.listAll();
        } else {
            all = menuMapper.listByType(menuType);
        }

        // 内存中构建树形结构
        Map<Long, MenuVO> idMap = new HashMap<>();
        for (MenuVO m : all) {
            m.setChildren(new ArrayList<>());
            idMap.put(m.getId(), m);
        }

        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO m : all) {
            Long pid = m.getParentId();
            if (pid == null || pid == 0L || !idMap.containsKey(pid)) {
                roots.add(m);
            } else {
                idMap.get(pid).getChildren().add(m);
            }
        }
        return roots;
    }

    @Override
    @Transactional
    public void create(MenuDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("菜单名称不能为空");
        }
        if (dto.getMenuType() == null
                || (dto.getMenuType() != 1 && dto.getMenuType() != 2)) {
            throw new RuntimeException("菜单类型不合法");
        }
        // parentId 默认 0
        if (dto.getParentId() == null) {
            dto.setParentId(0L);
        } else if (dto.getParentId() != 0L) {
            SysMenu parent = menuMapper.getById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父菜单不存在");
            }
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        menuMapper.insert(menu);
        log.info("新增菜单成功，id={}", menu.getId());
    }

    @Override
    @Transactional
    public void update(Long id, MenuDTO dto) {
        SysMenu exist = menuMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("菜单不存在");
        }

        // 不能把自己设为自己的父菜单
        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new RuntimeException("父菜单不能是自己");
        }
        // 父菜单存在性校验
        if (dto.getParentId() != null && dto.getParentId() != 0L) {
            SysMenu parent = menuMapper.getById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父菜单不存在");
            }
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setId(id);
        menuMapper.update(menu);
        log.info("编辑菜单成功，id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysMenu exist = menuMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("菜单不存在");
        }
        // 有子菜单不允许删除
        int childCount = menuMapper.countChildren(id);
        if (childCount > 0) {
            throw new RuntimeException("请先删除子菜单");
        }
        menuMapper.deleteById(id);
        log.info("删除菜单成功，id={}", id);
    }
}
