package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 经纪公司表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agency implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 公司名
     */
    private String name;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
