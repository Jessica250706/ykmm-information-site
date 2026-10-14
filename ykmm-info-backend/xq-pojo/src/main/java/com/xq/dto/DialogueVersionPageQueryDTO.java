package com.xq.dto;

import com.xq.constant.DialogueSourceTypeConstant;
import com.xq.constant.StatusConstant;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueLanguageEnum;
import com.xq.enums.DialogueScopeEnum;
import com.xq.validation.EnumValue;
import com.xq.validation.InIntValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 对话版本分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DialogueVersionPageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     */
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
    private Long sourceId;

    /**
     * 语言：1中文 2日文
     */
    @EnumValue(enumClass = DialogueLanguageEnum.class, message = "语言不合法")
    private Integer language;

    /**
     * 形式：1文字 2图片
     */
    @EnumValue(enumClass = DialogueFormatEnum.class, message = "形式不合法")
    private Integer format;

    /**
     * 范围：1全部 2节选
     */
    @EnumValue(enumClass = DialogueScopeEnum.class, message = "范围不合法")
    private Integer scope;

    /** 状态：1已发布 2待审核 3已拒绝（管理员可按状态筛选） */
    @InIntValues(value = {
            StatusConstant.PUBLISHED,
            StatusConstant.PENDING,
            StatusConstant.REJECTED,
    },
            message = "状态不合法")
    private Integer status;
}
