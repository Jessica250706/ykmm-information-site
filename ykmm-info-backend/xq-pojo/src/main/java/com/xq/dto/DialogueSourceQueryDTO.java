package com.xq.dto;

import com.xq.constant.DialogueSourceTypeConstant;
import com.xq.validation.InIntValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 * 对话来源查询参数
 * <p>
 * 用于"根据来源类型 + 来源ID查询对话内容"的通用场景
 */
@Data
@Getter
public class DialogueSourceQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     *
     * 注意：卡面附属剧情接口仅支持 1/2/3，剧情接口用 4。
     * 这里统一放开到 4，由 Service 内部按业务再判断（见下）。
     */
    @NotNull(message = "来源类型不能为空")
    @InIntValues(
            value = {DialogueSourceTypeConstant.RC,
                    DialogueSourceTypeConstant.RTV,
                    DialogueSourceTypeConstant.RABITTER,
                    DialogueSourceTypeConstant.STORY},
            message = "来源类型不合法")
    private Integer sourceType;

    /**
     * 来源ID
     */
    @NotNull(message = "来源ID不能为空")
    private Long sourceId;
}
