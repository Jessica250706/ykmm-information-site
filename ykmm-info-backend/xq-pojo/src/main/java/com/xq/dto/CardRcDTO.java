package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面 RC 新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRcDTO implements Serializable {
    /** 所属卡面ID */
    private Long cardId;
    /** RC发起人角色ID，该角色对话默认右侧（side=2） */
    private Long roleId;
    /** 第几话 */
    private Integer episodeNo;
    /** 标题 */
    private String title;
}
