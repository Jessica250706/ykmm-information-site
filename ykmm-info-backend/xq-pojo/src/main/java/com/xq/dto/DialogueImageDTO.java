package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话图片
 */
@Data
public class DialogueImageDTO implements Serializable {

    /**
     * 图片ID，编辑时使用
     */
    private Long id;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 排序
     */
    private Integer sort;
}
