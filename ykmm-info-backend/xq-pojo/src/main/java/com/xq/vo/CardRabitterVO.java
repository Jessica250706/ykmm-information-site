package com.xq.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡面 Rabitter 返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRabitterVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 所属卡面ID
     */
    private Long cardId;
    /**
     * 第几话
     */
    private Integer episodeNo;
    /**
     * 标题
     */
    private String title;
    /**
     * 创建时间
     */
    private String createdAt;
}
