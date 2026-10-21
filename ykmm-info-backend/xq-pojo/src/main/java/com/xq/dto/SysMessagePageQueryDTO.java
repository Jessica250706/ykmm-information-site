package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统消息分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessagePageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 接收者用户ID。
     * <p>
     * 仅管理员生效：管理员可按用户筛选；普通用户该字段会被 Service 忽略，
     * 强制查询自己的消息，防止越权。
     */
    private Long userId;

    /** 消息类型：1-审核通过 2-审核拒绝 3-系统通知 */
    private Integer type;

    /** 关联业务类型：1-卡面 2-对话版本 3-剧情 */
    private Integer targetType;

    /** 是否已读：0-未读 1-已读 */
    private Integer isRead;

    /** 关键字：标题 / 内容模糊匹配 */
    private String keyword;
}
