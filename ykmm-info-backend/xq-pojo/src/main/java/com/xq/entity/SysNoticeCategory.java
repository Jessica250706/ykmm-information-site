package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告分类字典表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysNoticeCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键（与代码枚举值对应，字典表不自动生成） */
    private Integer id;

    /** 分类名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 标签颜色：primary/success/warning/danger/info */
    private String color;

    /** 排序 */
    private Integer sort;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
