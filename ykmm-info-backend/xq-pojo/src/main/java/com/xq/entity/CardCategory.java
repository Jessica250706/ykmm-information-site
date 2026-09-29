package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面类别字典
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 类别名：生日、纪念日等
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;
}
