package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话版本选项（供下拉框使用）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DialogueVersionOptionVO {
    /**
     * 版本ID，当前来源下不存在时为 null
     */
    private Long versionId;
    /**
     * 是否已存在
     */
    private Boolean exists;
    /**
     * 语言：1中文 2日文
     */
    private Integer language;
    /**
     * 语言标签
     */
    private String languageLabel;
    /**
     * 格式：1文字 2图片
     */
    private Integer format;
    /**
     * 格式标签
     */
    private String formatLabel;
    /**
     * 范围：1全部 2节选
     */
    private Integer scope;
    /**
     * 范围标签
     */
    private String scopeLabel;
    /**
     * 完整展示文案，如「中文 · 文字 · 全部」
     */
    private String label;
}
