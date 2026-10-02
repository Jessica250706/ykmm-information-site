package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧情审核
 */
@Data
public class StoryAuditDTO implements Serializable {

    /**
     * 审核结果：1通过 3拒绝
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String reviewRemark;
}
