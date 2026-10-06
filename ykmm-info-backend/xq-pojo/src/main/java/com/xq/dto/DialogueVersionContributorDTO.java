package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话版本贡献者新增/编辑参数
 */
@Data
public class DialogueVersionContributorDTO implements Serializable {

    /**
     * 对话版本ID
     */
    private Long versionId;

    /**
     * 贡献者用户ID，可为空（无账号时只填 contributorName）
     */
    private Long userId;

    /**
     * 无账号贡献者姓名，userId 为空时必填
     */
    private String contributorName;

    /**
     * 贡献者角色：1内容作者 2代传管理员 3协作者
     */
    private Integer contributorRole;
}
