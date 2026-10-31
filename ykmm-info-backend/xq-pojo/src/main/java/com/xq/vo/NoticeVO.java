package com.xq.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告 VO
 */
@Data
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;

    private Integer categoryId;
    /** 分类名称 */
    private String categoryName;
    /** 分类颜色 */
    private String categoryColor;

    private String summary;
    private String content;
    private String coverImage;

    private Integer status;
    /** 状态标签：草稿/已发布/已下线 */
    private String statusLabel;

    private Integer minVisibility;
    /** 可见性标签 */
    private String minVisibilityLabel;

    private Integer isTop;
    private LocalDateTime publishAt;
    private LocalDateTime expireAt;

    private Long creatorId;
    /** 创建者昵称（优先昵称，回退邮箱） */
    private String creatorName;

    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
