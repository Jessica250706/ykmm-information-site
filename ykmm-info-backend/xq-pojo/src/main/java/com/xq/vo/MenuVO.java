package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 前端组件
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 1管理端 2用户端
     */
    private Integer menuType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否显示
     */
    private Integer visible;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 子菜单，构建树时使用
     */
    private List<MenuVO> children;
}
