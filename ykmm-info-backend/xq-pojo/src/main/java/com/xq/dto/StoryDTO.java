package com.xq.dto;

import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 剧情新增/编辑
 */
@Data
public class StoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属分类节点
     *
     * - 新增：必填
     * - 编辑：可空（不传=不改）
     */
    @NotNull(message = "所属分类不能为空", groups = CreateGroup.class)
    private Long categoryId;

    /**
     * 话标题
     *
     * - 新增：非空，长度 1~255
     * - 编辑：可空；传了则长度 1~255
     *
     * @NotBlank 只在 CreateGroup 生效；
     * @Size 允许 null，同时覆盖 create 和 update。
     */
    @NotBlank(message = "话标题不能为空", groups = CreateGroup.class)
    @Size(max = 255, message = "话标题不能超过 255 个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
