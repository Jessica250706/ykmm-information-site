package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 人物-公司关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonAgencyRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 人物ID */
    private Long personId;

    /** 公司ID */
    private Long agencyId;
}
