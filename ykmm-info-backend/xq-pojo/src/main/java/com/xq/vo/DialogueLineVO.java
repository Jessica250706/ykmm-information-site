package com.xq.vo;

import lombok.Data;

import java.util.List;

/**
 * 对话句子
 */
@Data
public class DialogueLineVO {
    private Long id;
    private Long speakerId;
    private String speakerName;
    private String speakerAvatar;
    /**
     * 1左 2右
     */
    private Integer side;
    private String content;
    private Integer sort;
    private List<DialogueSegmentVO> segments;
}
