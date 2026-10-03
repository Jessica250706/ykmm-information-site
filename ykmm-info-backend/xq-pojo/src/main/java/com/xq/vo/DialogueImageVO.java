package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对话图片返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueImageVO implements Serializable {

    /**
     * 主键
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
