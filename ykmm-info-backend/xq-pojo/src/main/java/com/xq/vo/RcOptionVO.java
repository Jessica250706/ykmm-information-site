package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RC 选项返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RcOptionVO implements Serializable {
    /** 主键 */
    private Long id;
    /** RC ID */
    private Long rcId;
    /** 版本ID */
    private Long versionId;
    /** 问句ID */
    private Long questionLineId;
    /** 答句ID */
    private Long answerLineId;
    /** 排序 */
    private Integer sort;
}
