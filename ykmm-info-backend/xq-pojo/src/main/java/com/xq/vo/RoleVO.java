package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 对应人物ID，可为空
     */
    private Long personId;

    /**
     * 对应人物中文名，可为空
     */
    private String personNameCn;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色简介
     */
    private String intro;

    /**
     * 所属剧情分类根节点
     */
    private List<StoryCategoryVO> storyCategories;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
