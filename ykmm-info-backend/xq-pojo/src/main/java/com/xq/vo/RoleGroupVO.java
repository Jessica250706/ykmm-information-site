package com.xq.vo;

import lombok.Data;

import java.util.List;

/**
 * 角色分组（按人物）
 */
@Data
public class RoleGroupVO {
    /**
     * 人物ID，null 表示"其他"分组
     */
    private Long personId;
    /**
     * 人物中文名，或"其他"
     */
    private String personName;
    /**
     * 人物头像
     */
    private String personAvatar;
    /**
     * 该人物下的角色列表
     */
    private List<RoleSimpleVO> roles;
}
