package com.xq.dto;

import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡面类别新增/编辑参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCategoryDTO implements Serializable {

    /**
     * 类别名
     * <p>新增必填，长度 1~64；编辑可为 null（不改）
     */
    @NotBlank(message = "类别名称不能为空", groups = CreateGroup.class)
    @Size(min = 1, max = 64,
            message = "类别名称长度需在 1~64 个字符之间",
            groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 排序：值越小越靠前；不传默认 0
     */
    private Integer sort;
}
