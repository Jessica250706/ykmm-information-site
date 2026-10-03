package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话版本分页查询条件
 */
@Data
public class DialogueVersionPageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 来源类型：1剧情 2卡面RTV 3卡面RC
     */
    private Integer sourceType;

    /**
     * 来源ID
     */
    private Long sourceId;

    /**
     * 语言：1中文 2日文
     */
    private Integer language;

    /**
     * 形式：1文字 2图片
     */
    private Integer format;

    /**
     * 范围：1全部 2节选
     */
    private Integer scope;
}
