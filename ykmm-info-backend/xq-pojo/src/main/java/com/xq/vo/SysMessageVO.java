package com.xq.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统消息 VO
 */
@Data
public class SysMessageVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 接收者用户ID，关联 sys_user.id
     */
    private Long userId;

    /** 消息类型：1-审核通过 2-审核拒绝 3-系统通知 */
    private Integer type;

    /** 消息类型标签，如「审核通过」 */
    private String typeLabel;

    /** 关联业务类型：1-卡面 2-对话版本 3-剧情 */
    private Integer targetType;

    /** 关联业务类型标签，如「卡面」 */
    private String targetTypeLabel;

    /**
     * 关联业务主键
     */
    private Long targetId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文（生成时的快照）
     */
    private String content;

    /** 0-未读 1-已读 */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
