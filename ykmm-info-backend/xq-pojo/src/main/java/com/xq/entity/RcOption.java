package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RC选项表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RcOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** RC ID */
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
