package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * txt 解析结果返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueTxtParseVO implements Serializable {

    /**
     * 全部识别到的说话人名称
     */
    private List<String> speakers;

    /**
     * 未匹配到角色的说话人
     */
    private List<String> unmatchedSpeakers;

    /**
     * 未匹配到表情包的标签
     */
    private List<String> unmatchedStickers;

    /**
     * 解析出的句子
     */
    private List<DialogueLineVO> lines;

    /**
     * 错误信息
     */
    private List<String> errors;
}
