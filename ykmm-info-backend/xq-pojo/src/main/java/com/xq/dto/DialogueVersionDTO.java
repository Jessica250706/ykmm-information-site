package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话版本创建
 */
@Data
public class DialogueVersionDTO implements Serializable {

    /**
     * 1剧情 2卡面RTV 3卡面RC
     */
    private Integer sourceType;

    /**
     * 对应 story.id / card_rtv.id / card_rc.id
     */
    private Long sourceId;

    /**
     * 1中文 2日文
     */
    private Integer language;

    /**
     * 1文字 2图片
     */
    private Integer format;

    /**
     * 1全部 2节选
     */
    private Integer scope;

    /**
     * 贡献者用户ID，可为空（无账号时只保留名字）
     */
    private Long contributorUserId;

    /**
     * 无账号贡献者姓名（user_id 为空时使用）
     */
    private String contributorName;
}
