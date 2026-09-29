package com.xq.mapper;

import com.xq.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    /**
     * 查询全部菜单
     */
    List<SysMenu> listAll();

    /**
     * 按菜单类型查询
     */
    List<SysMenu> listByType(@Param("menuType") Integer menuType);

    /**
     * 根据 id 查询
     */
    SysMenu getById(@Param("id") Long id);

    /**
     * 统计子菜单数量
     */
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 新增菜单
     */
    int insert(SysMenu menu);

    /**
     * 更新菜单
     */
    int update(SysMenu menu);

    /**
     * 删除菜单
     */
    int deleteById(@Param("id") Long id);
}