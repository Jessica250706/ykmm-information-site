package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 偶像团体表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdolGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 团体名 */
    private String name;

    /** 所属经纪公司 */
    private Long agencyId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
