package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 故事贡献者（按用户 / 姓名聚合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryContributorVO implements Serializable {

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
     * 展示名：优先昵称，其次姓名，都没有则"匿名"
     */
    private String displayName;

    /**
     * 贡献的版本数
     */
    private Integer versionCount;
}
