package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 偶像小人表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chibi implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 名称 */
    private String name;

    /** 图片地址 */
    private String imageUrl;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
