package com.xq.dto;

import com.xq.validation.CreateGroup;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 卡面 Rabitter 新增/编辑 DTO
 */
@Data
public class CardRabitterDTO {

    /** 卡面ID（仅新增时必填） */
    @NotNull(groups = CreateGroup.class, message = "卡面ID不能为空")
    private Long cardId;

    /** 第几话（仅新增时必填） */
    @NotNull(groups = CreateGroup.class, message = "话数不能为空")
    private Integer episodeNo;

    /** 话标题 */
    @Size(max = 255, message = "标题最多 255 个字符")
    private String title;
}
