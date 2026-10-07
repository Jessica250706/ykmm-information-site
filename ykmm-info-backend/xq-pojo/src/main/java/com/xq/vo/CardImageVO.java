package com.xq.vo;

import com.xq.entity.CardImage;
import com.xq.entity.SysUser;
import com.xq.enums.CardImageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面图片返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardImageVO implements Serializable {
    /** 主键 */
    private Long id;
    /** 图片类型 */
    private Integer imageType;
    /** 图片类型标签 */
    private String imageTypeLabel;
    /** 图片地址 */
    private String url;
    /** 排序 */
    private Integer sort;

    /**
     * 从 CardImage 实体构建 CardImageVO（用于脱敏后返回给前端）
     */
    public static CardImageVO from(CardImage cardImage) {
        if (cardImage == null) {
            return null;
        }
        return CardImageVO.builder()
                .id(cardImage.getId())
                .imageType(cardImage.getImageType())
                .imageTypeLabel(CardImageTypeEnum.getLabel(cardImage.getImageType()))
                .sort(cardImage.getSort())
                .build();
    }
}
