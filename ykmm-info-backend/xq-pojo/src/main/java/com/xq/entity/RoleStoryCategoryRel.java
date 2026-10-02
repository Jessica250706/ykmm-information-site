package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色-剧情分类根节点关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleStoryCategoryRel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 剧情分类根节点ID
     */
    private Long storyCategoryId;
}
