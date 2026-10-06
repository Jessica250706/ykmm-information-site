package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话版本贡献者
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueVersionContributor {

    /**
     * 主键
     */
    private Long id;

    /**
     * 对话版本ID
     */
    private Long versionId;

    /**
     * 贡献者用户ID，可为空（无账号时只保留名字）
     */
    private Long userId;

    /**
     * 无账号贡献者姓名（user_id 为空时使用）
     */
    private String contributorName;

    /**
     * 贡献者角色：1内容作者 2代传管理员 3协作者
     */
    private Integer contributorRole;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
