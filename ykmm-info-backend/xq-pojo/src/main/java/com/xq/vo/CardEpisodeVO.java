package com.xq.vo;

import lombok.Data;

import java.util.List;

/**
 * 卡面附属剧情 - 一话 VO
 */
@Data
public class CardEpisodeVO {

    /**
     * 话ID（card_rc.id 或 card_rtv.id）
     */
    private Long id;

    /**
     * 卡面ID
     */
    private Long cardId;

    /**
     * 第几话
     */
    private Integer episodeNo;

    /**
     * 话标题
     */
    private String title;

    /**
     * 发起人角色ID（仅 RC）
     */
    private Long initiatorRoleId;

    /**
     * 发起人角色名（仅 RC）
     */
    private String initiatorRoleName;

    /**
     * 该话下的所有对话版本
     */
    private List<DialogueVersionVO> versions;
}
