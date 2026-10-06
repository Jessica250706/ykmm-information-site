package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RC 选项新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RcOptionDTO implements Serializable {
    /** 所属 RC ID */
    private Long rcId;
    /** 所属对话版本 */
    private Long versionId;
    /** 发起者问的句子ID */
    private Long questionLineId;
    /** 参与角色回答的句子ID */
    private Long answerLineId;
    /** 排序 */
    private Integer sort;
}
