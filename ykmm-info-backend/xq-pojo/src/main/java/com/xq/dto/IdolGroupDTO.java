package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 偶像团体新增/编辑
 */
@Data
public class IdolGroupDTO implements Serializable {

    /**
     * 团体名
     */
    private String name;

    /**
     * 所属经纪公司ID
     */
    private Long agencyId;
}