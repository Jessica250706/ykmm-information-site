package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面 RTV 新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRtvDTO implements Serializable {
    /** 所属卡面ID */
    private Long cardId;
    /** 第几话 */
    private Integer episodeNo;
    /** 标题 */
    private String title;
}
