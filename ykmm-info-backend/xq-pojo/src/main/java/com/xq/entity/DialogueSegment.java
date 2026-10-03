package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对话片段表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueSegment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属对话句子
     */
    private Long lineId;

    /**
     * 1文本 2表情包
     */
    private Integer segmentType;

    /**
     * 文本内容，segment_type=1时使用
     */
    private String content;

    /**
     * 表情包ID，segment_type=2时使用
     */
    private Long stickerId;

    /**
     * 片段顺序
     */
    private Integer sort;
}
