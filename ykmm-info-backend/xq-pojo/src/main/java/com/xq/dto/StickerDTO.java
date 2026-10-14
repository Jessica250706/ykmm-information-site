package com.xq.dto;

import com.xq.enums.StickerTypeEnum;
import com.xq.validation.CreateGroup;
import com.xq.validation.EnumValue;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 表情包新增/更新DTO
 */
@Data
public class StickerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     * <p>
     * 新增：忽略
     * 编辑：由路径变量 {id} 填充，DTO 不约束
     */
    private Long id;

    /**
     * 所属分组ID
     * <p>
     * 新增、编辑都必填
     */
    @NotNull(message = "分组ID不能为空",
            groups = {CreateGroup.class, UpdateGroup.class})
    private Long groupId;

    /**
     * 标签
     * <p>
     * 新增：必填，长度 1~64
     * 编辑：可空（不传=不改）；传了则长度 1~64
     */
    @NotBlank(message = "标签不能为空", groups = CreateGroup.class)
    @Size(min = 1, max = 64,
            message = "标签长度需在 1~64 个字符之间",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String label;

    /**
     * 图片地址
     * <p>
     * 长度校验两种场景共用；
     * "stickerType=1 时必填" 属于跨字段条件校验，保留在 Service 中。
     */
    @Size(max = 512, message = "图片地址不能超过512个字符",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String imageUrl;

    /**
     * 对应emoji
     * <p>
     * 长度校验两种场景共用；
     * "stickerType=2 时必填" 属于跨字段条件校验，保留在 Service 中。
     */
    @Size(max = 32, message = "emoji不能超过32个字符",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String emoji;

    /**
     * 1自定义图片 2 emoji
     * <p>
     * 新增、编辑都必填，且必须是合法枚举值
     */
    @NotNull(message = "表情包类型不能为空",
            groups = {CreateGroup.class, UpdateGroup.class})
    @EnumValue(enumClass = StickerTypeEnum.class,
            message = "表情包类型不正确",
            groups = {CreateGroup.class, UpdateGroup.class})
    private Integer stickerType;
}
