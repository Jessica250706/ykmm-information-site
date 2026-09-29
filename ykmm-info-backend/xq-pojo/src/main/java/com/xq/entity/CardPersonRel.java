package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面-人物关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPersonRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 卡面ID */
    private Long cardId;

    /** 人物ID */
    private Long personId;
}
