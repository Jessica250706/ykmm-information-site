package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 经纪公司新增/编辑
 */
@Data
public class AgencyDTO implements Serializable {

    /**
     * 公司名
     */
    private String name;
}