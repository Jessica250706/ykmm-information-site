package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话版本返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueVersionVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 来源类型
     */
    private Integer sourceType;

    /**
     * 来源类型标签
     */
    private String sourceTypeLabel;

    /**
     * 来源ID
     */
    private Long sourceId;

    /**
     * 语言
     */
    private Integer language;

    /**
     * 语言标签
     */
    private String languageLabel;

    /**
     * 形式
     */
    private Integer format;

    /**
     * 形式标签
     */
    private String formatLabel;

    /**
     * 范围
     */
    private Integer scope;

    /**
     * 范围标签
     */
    private String scopeLabel;

    /**
     * 文字版本时的句子列表
     */
    private List<DialogueLineVO> lines;

    /**
     * 图片版本时的图片列表
     */
    private List<DialogueImageVO> images;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
