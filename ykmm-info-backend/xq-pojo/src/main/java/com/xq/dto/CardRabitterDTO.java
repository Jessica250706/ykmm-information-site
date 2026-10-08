package com.xq.dto;

import lombok.Data;

/**
 * 卡面 Rabitter 新增/编辑 DTO
 */
@Data
public class CardRabitterDTO {

    /** 卡面ID */
    private Long cardId;

    /** 第几话 */
    private Integer episodeNo;

    /** 话标题 */
    private String title;
}
