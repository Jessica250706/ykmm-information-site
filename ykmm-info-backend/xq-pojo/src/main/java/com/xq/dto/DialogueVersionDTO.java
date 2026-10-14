package com.xq.dto;

import com.xq.constant.DialogueSourceTypeConstant;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueLanguageEnum;
import com.xq.enums.DialogueScopeEnum;
import com.xq.validation.EnumValue;
import com.xq.validation.InIntValues;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 对话版本创建
 */
@Data
public class DialogueVersionDTO implements Serializable {

    /**
     * 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     */
    @NotNull(message = "来源类型不能为空")
    @InIntValues(value = {
            DialogueSourceTypeConstant.RC,
            DialogueSourceTypeConstant.RTV,
            DialogueSourceTypeConstant.RABITTER,
            DialogueSourceTypeConstant.STORY
    },
            message = "来源类型不合法")
    private Integer sourceType;

    /**
     * 来源ID：对应 story.id / card_rtv.id / card_rc.id
     */
    @NotNull(message = "来源ID不能为空")
    private Long sourceId;

    /**
     * 语言：1中文 2日文
     */
    @NotNull(message = "语言不能为空")
    @EnumValue(enumClass = DialogueLanguageEnum.class, message = "语言不合法")
    private Integer language;

    /**
     * 形式：1文字 2图片
     */
    @NotNull(message = "形式不能为空")
    @EnumValue(enumClass = DialogueFormatEnum.class, message = "形式不合法")
    private Integer format;

    /**
     * 范围：1全部 2节选
     */
    @NotNull(message = "范围不能为空")
    @EnumValue(enumClass = DialogueScopeEnum.class, message = "范围不合法")
    private Integer scope;

    /**
     * 贡献者用户ID，可为空（无账号时只保留名字）
     */
    private Long contributorUserId;

    /**
     * 无账号贡献者姓名（user_id 为空时使用）
     */
    @Size(max = 64, message = "贡献者姓名不能超过 64 个字符")
    private String contributorName;
}
