package com.xq.dto;

import com.xq.validation.CreateGroup;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(groups = CreateGroup.class, message = "卡面ID不能为空")
    private Long cardId;
    /** 第几话 */
    @NotNull(groups = CreateGroup.class, message = "话数不能为空")
    private Integer episodeNo;
    /** 标题 */
    @Size(max = 255, message = "标题最多 255 个字符")
    private String title;
}
