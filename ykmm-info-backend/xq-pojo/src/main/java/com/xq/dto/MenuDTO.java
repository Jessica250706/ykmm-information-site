package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuDTO implements Serializable {

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
}
