package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话句子
 */
@Data
public class DialogueLineDTO implements Serializable {

    /**
     * 句子ID，编辑时使用，新增时为空
     */
    private Long id;

    /**
     * 说话角色ID
     */
    private Long speakerId;

    /**
     * RC聊天：1左 2右，非RC可为空
     */
    private Integer side;

    /**
     * 文本内容，可含表情包标签
     */
    private String content;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否内心独白：0否 1是
     */
    private Integer monologue;

    /**
     * 对话角色：0-普通 1-问句 2-回答
     */
    private Integer dialogueRole;

    /**
     * 选项编号（仅 RC 的问句/回答有效）
     */
    private Integer optionNumber;

}
