package com.xq.dto;

import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 表情包分组新增/更新DTO
 */
@Data
public class StickerGroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     * <p>
     * - 新增：忽略（由数据库自增生成）
     * - 编辑：必填（Service 里也会 set，但加个注解兜底）
     */
    @NotNull(message = "分组ID不能为空", groups = UpdateGroup.class)
    private Long id;

    /**
     * 分组名称
     * <p>
     * - 新增：必填，非空
     * - 编辑：可空（不传=不改）；传了则长度 1~64
     */
    @NotBlank(message = "分组名称不能为空", groups = CreateGroup.class)
    @Size(min = 1, max = 64,
            message = "分组名称长度需在 1~64 个字符之间",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 分组描述
     */
    @Size(max = 255, message = "分组描述不能超过255个字符",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
