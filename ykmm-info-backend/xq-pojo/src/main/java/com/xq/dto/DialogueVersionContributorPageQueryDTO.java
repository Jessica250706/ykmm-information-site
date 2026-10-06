package com.xq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话版本贡献者分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DialogueVersionContributorPageQueryDTO extends PageQueryDTO {

    /**
     * 对话版本ID
     */
    private Long versionId;

    /**
     * 贡献者用户ID
     */
    private Long userId;

    /**
     * 贡献者角色：1内容作者 2代传管理员 3协作者
     */
    private Integer contributorRole;
}
