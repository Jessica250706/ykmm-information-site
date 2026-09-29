package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuDTO implements Serializable {

    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;

    /**
     * 1管理端 2用户端
     */
    private Integer menuType;

    private Integer sort;
    private Integer visible;
    private String permission;
}
