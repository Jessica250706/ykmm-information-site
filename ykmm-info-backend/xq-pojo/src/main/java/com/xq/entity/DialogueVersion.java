package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话版本表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 1-RC 2-RTV 3-Rabitter 4-剧情
     */
    private Integer sourceType;

    /**
     * 对应 card_rc.id / card_rtv.id / card_rabitter.id / story.id
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
     * 状态
     */
    private Long status;

    /**
     * 审批ID
     */
    private Long reviewerId;

    /**
     * 审批时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审批理由
     */
    private String reviewRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 实际操作人ID：谁调用了创建接口（可能是管理员代传）
     */
    private Long creatorId;

    /**
     * 创建时的角色：1-管理员 2-普通用户
     */
    private Integer creatorRole;
}
