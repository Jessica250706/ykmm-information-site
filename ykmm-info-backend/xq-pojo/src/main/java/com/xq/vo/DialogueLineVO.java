package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 对话句子返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueLineVO implements Serializable {

    /**
     * 句子ID
     */
    private Long id;

    /**
     * 版本ID
     */
    private Long versionId;

    /**
     * 说话角色ID
     */
    private Long speakerId;

    /**
     * 说话角色名
     */
    private String speakerName;

    /**
     * 对应人物ID
     */
    private Long personId;

    /**
     * 对应人物中文名
     */
    private String personNameCn;

    /**
     * 对应人物头像
     */
    private String personAvatar;

    /**
     * 应援色
     */
    private String personThemeColor;

    /**
     * RC聊天：1左 2右
     */
    private Integer side;

    /**
     * 是否内心独白：0否 1是
     */
    private Integer monologue;

    /**
     * 原始文本内容
     */
    private String content;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 对话角色：0-普通 1-问句 2-回答
     */
    private Integer dialogueRole;

    /**
     * 选项编号（仅 RC 的问句/回答有效）
     */
    private Integer optionNumber;

    /**
     * 对话角色标签（"普通" / "问句" / "回答"）
     */
    private String dialogueRoleLabel;

    /**
     * 解析后的片段（文本 + 表情包混排）
     */
    private List<DialogueSegmentVO> segments;
}
