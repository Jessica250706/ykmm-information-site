package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面关联人物
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPersonVO implements Serializable {
    /** 人物ID */
    private Long personId;
    /** 中文名 */
    private String nameCn;
    /** 头像 */
    private String avatar;
}
