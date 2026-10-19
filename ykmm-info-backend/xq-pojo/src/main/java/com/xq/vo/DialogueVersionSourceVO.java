package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话版本的来源展示信息
 * <p>
 * 用于"我的版本"等需要展示来源详情的列表场景，
 * 让前端一次请求拿到展示 + 跳转所需的全部信息。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DialogueVersionSourceVO {

    /** 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情 */
    private Integer sourceType;

    /** 来源ID：card_rc.id / card_rtv.id / card_rabitter.id / story.id */
    private Long sourceId;

    /**
     * 主标题：
     * - card 类：卡面名称
     * - story 类：剧情话标题
     */
    private String title;

    /**
     * 副标题：
     * - card 类：卡面系列名
     * - story 类：剧情分类名
     */
    private String subtitle;

    /** 话数（仅 card 类） */
    private Integer episodeNo;

    /** 话标题（仅 card 类） */
    private String episodeTitle;

    /** 所属卡面ID（仅 card 类，便于前端跳转） */
    private Long cardId;

    /** 剧情分类类型（仅 story 类，便于前端拼路由 params.type） */
    private Integer categoryType;
}
