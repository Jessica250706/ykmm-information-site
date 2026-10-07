package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * 卡面关联人物
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPersonVO implements Serializable {
    /**
     * 卡面ID（仅用于批量查询分组，不返回给前端）
     */
    @JsonIgnore
    private Long cardId;

    /**
     * 人物ID
     */
    private Long personId;

    /**
     * 中文名
     */
    private String nameCn;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 代表色
     */
    private String themeColor;
}
