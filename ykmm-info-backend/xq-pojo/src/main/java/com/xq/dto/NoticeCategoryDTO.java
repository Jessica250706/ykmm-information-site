package com.xq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 公告分类新增/编辑参数
 * <p>
 * 字典表的 id 需要手动指定（与代码枚举值对应），新增和编辑都用此 DTO。
 */
@Data
public class NoticeCategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID（新增时必填；编辑时忽略，以路径参数为准） */
    @NotNull(message = "分类ID不能为空")
    private Integer id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称最多 32 个字符")
    private String name;

    @Size(max = 255, message = "描述最多 255 个字符")
    private String description;

    @Size(max = 32, message = "颜色最多 32 个字符")
    private String color;

    private Integer sort;
}
