package com.xq.vo;

import lombok.Data;

/**
 * 对话片段
 */
@Data
public class DialogueSegmentVO {
    private Long id;
    /**
     * 1文本 2表情包
     */
    private Integer segmentType;
    private String content;
    private Long stickerId;
    private String stickerUrl;
    private String stickerEmoji;
    private Integer sort;
}
