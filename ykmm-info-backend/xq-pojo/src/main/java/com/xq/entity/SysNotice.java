package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 标题 */
    private String title;

    /** 分类ID，关联 sys_notice_category.id */
    private Integer categoryId;

    /** 摘要（列表展示用；可为空） */
    private String summary;

    /** 富文本内容（HTML 片段） */
    private String content;

    /** 封面图 URL（可选） */
    private String coverImage;

    /** 状态：0-草稿 1-已发布 2-已下线 */
    private Integer status;

    /** 最低可见级别：1-游客及以上 2-用户及以上 3-仅管理员 */
    private Integer minVisibility;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 发布时间（支持定时发布；为空时按 created_at 算） */
    private LocalDateTime publishAt;

    /** 过期时间（为空表示永不过期） */
    private LocalDateTime expireAt;

    /** 创建者用户ID */
    private Long creatorId;

    /** 浏览量 */
    private Integer viewCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
