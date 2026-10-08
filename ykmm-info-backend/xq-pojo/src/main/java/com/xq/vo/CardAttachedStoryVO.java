package com.xq.vo;

import lombok.Data;

import java.util.List;

/**
 * 卡面附属剧情 VO
 */
@Data
public class CardAttachedStoryVO {

    /**
     * 附属剧情类型：1-RC 2-RTV 3-Rabitter
     */
    private Integer storyType;

    /**
     * 类型标签
     */
    private String storyTypeLabel;

    /**
     * 话列表
     */
    private List<CardEpisodeVO> episodes;
}
