package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对话片段返回（文本 / 表情包）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueSegmentVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 行ID
     */
    private Long lineId;

    /**
     * 片段类型：1文本 2表情包
     */
    private Integer segmentType;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 表情包ID
     */
    private Long stickerId;

    /**
     * 表情包标签
     */
    private String stickerLabel;

    /**
     * 表情包图片
     */
    private String stickerImageUrl;

    /**
     * 表情包 emoji
     */
    private String stickerEmoji;

    /**
     * 顺序
     */
    private Integer sort;
}
