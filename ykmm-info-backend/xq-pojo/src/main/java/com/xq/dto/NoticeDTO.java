package com.xq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告新增/编辑参数
 */
@Data
public class NoticeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题最多 255 个字符")
    private String title;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @Size(max = 500, message = "摘要最多 500 个字符")
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 512, message = "封面图 URL 最多 512 个字符")
    private String coverImage;

    @NotNull(message = "可见性不能为空")
    private Integer minVisibility;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 发布时间；为空时按创建时间算 */
    private LocalDateTime publishAt;

    /** 过期时间；为空时永不过期 */
    private LocalDateTime expireAt;
}
