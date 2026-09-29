package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 人物-团体关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonGroupRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 人物ID */
    private Long personId;

    /** 团体ID */
    private Long groupId;
}
