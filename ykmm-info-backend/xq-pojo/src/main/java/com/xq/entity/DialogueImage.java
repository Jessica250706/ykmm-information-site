package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对话图片表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 版本ID */
    private Long versionId;

    /** 图片地址 */
    private String url;

    /** 排序 */
    private Integer sort;
}
