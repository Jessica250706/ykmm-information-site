package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧情分类简要信息
 */
@Data
public class StoryCategorySimpleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分类名
     */
    private String name;

    /**
     * 分类类型
     */
    private Integer categoryType;
}
