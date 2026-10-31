package com.xq.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 公告分类 VO
 */
@Data
public class NoticeCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String description;
    private String color;
    private Integer sort;
}
