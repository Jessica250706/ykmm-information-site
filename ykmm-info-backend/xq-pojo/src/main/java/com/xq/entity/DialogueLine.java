package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话句子表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueLine implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 版本ID
     */
    private Long versionId;

    /**
     * 说话人物ID，关联 role.id
     */
    private Long speakerId;

    /**
     * RC聊天：1左 2右
     */
    private Integer side;

    /**
     * 纯文本内容缓存
     */
    private String content;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
