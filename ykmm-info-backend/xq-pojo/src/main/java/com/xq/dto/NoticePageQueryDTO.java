package com.xq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公告分页查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NoticePageQueryDTO extends PageQueryDTO implements Serializable {

    /** 关键词：标题模糊匹配 */
    private String keyword;

    /** 分类ID */
    private Integer categoryId;

    /** 状态（仅管理端生效）：0-草稿 1-已发布 2-已下线 */
    private Integer status;

    /** 最低可见级别（仅管理端生效） */
    private Integer minVisibility;

    /** 是否置顶（仅管理端生效） */
    private Integer isTop;
}
