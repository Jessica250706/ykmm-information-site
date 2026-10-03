package com.xq.vo;

import lombok.Data;

import java.util.List;

/**
 * 对话版本
 */
@Data
public class DialogueVersionVO {
    private Long id;
    /**
     * 1中文 2日文
     */
    private Integer language;
    private String languageLabel;
    /**
     * 1文字 2图片
     */
    private Integer format;
    private String formatLabel;
    /**
     * 1全部 2节选
     */
    private Integer scope;
    private String scopeLabel;
    private List<DialogueLineVO> lines;
    private List<DialogueImageVO> images;
}
