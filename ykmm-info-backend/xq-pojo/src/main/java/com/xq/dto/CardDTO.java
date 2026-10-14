package com.xq.dto;

import com.xq.enums.CardAttributeEnum;
import com.xq.enums.CardCostumeTypeEnum;
import com.xq.enums.CardMaxRarityEnum;
import com.xq.enums.DialogueSourceTypeEnum;
import com.xq.validation.CreateGroup;
import com.xq.validation.EnumValue;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 卡面新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO implements Serializable {

    /**
     * 卡面名称
     * <p>
     * - 新增（CreateGroup）：非空，长度 1~128
     * - 编辑（UpdateGroup）：可为 null（不改），传了则长度 1~128（非空字符串）
     */
    @NotBlank(message = "卡面名称不能为空", groups = CreateGroup.class)
    @Size(min = 1, max = 128,
            message = "卡面名称长度需在 1~128 个字符之间",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 所属系列ID
     */
    private Long seriesId;

    /**
     * 关联 card_category.id
     */
    private Integer category;

    /**
     * 最高等级：1-SSR 2-UR
     * <p>
     * - 新增：非空，且值必须合法
     * - 编辑：可空；非空时值必须合法
     * <p>
     * 说明：@EnumValue(nullable = true) 允许 null；@NotNull(groups = CreateGroup)
     * 只在新增场景强制非空，正好覆盖两种场景。
     */
    @NotNull(message = "卡面最高等级不能为空", groups = CreateGroup.class)
    @EnumValue(enumClass = CardMaxRarityEnum.class, nullable = true,
            message = "卡面最高等级不合法",
            groups = {CreateGroup.class, UpdateGroup.class})
    private Integer maxRarity;

    /**
     * 首次入池时间
     */
    private LocalDate firstPoolTime;

    /**
     * 属性：1-Shout 2-Beat 3-Melody
     */
    @EnumValue(enumClass = CardAttributeEnum.class, nullable = true,
            message = "属性不合法",
            groups = {CreateGroup.class, UpdateGroup.class})
    private Integer attribute;

    /**
     * 魅力技能描述
     */
    private String skillDesc;

    /**
     * 附属剧情类型：0无 1RC 2RTV 3Rabbiter
     */
    @EnumValue(enumClass = DialogueSourceTypeEnum.class, nullable = true,
            message = "附属剧情类型不合法",
            groups = {CreateGroup.class, UpdateGroup.class})
    private Integer attachedStoryType;

    /**
     * 服装类型：1偶像小人 2 3D造型
     */
    @EnumValue(enumClass = CardCostumeTypeEnum.class, nullable = true,
            message = "服装类型不合法",
            groups = {CreateGroup.class, UpdateGroup.class})
    private Integer costumeType;

    /**
     * 偶像小人ID
     */
    private Long chibiId;

    /**
     * 造型ID
     */
    private Long costumeId;

    /**
     * 关联人物ID列表
     */
    private List<Long> personIds;

    /**
     * 卡面图片列表
     */
    private List<CardImageDTO> images;
}
