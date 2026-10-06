package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对话版本贡献者返回
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DialogueVersionContributorVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 对话版本ID
     */
    private Long versionId;

    /**
     * 贡献者用户ID，可为空
     */
    private Long userId;

    /**
     * 用户随机标识
     */
    private String uid;

    /**
     * 昵称（有账号时）
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 无账号贡献者姓名
     */
    private String contributorName;

    /**
     * 展示名：优先昵称，其次姓名，都没有则用"匿名"
     */
    private String displayName;

    /**
     * 贡献者角色
     */
    private Integer contributorRole;

    /**
     * 贡献者角色标签
     */
    private String contributorRoleLabel;

    /**
     * 创建时间
     */
    private String createdAt;
}
