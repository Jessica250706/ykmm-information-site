package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息表
 * <p>
 * 用途：管理员审核用户提交的卡面 / 对话版本后，向用户发送结果通知；
 * 未来也可用于其他系统级通知（公告、回复等）。
 * <p>
 * 设计原则：
 * <ul>
 *   <li>消息一旦生成即为"快照"，title / content 不会因业务数据变化而变化；
 *       即使原卡面 / 版本被删除、审核备注被清空，用户仍能看到完整的历史消息</li>
 *   <li>target_type + target_id 用于前端精准跳转到对应业务详情</li>
 *   <li>is_read / read_at 支持未读统计与阅读时间追踪</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 接收者用户ID，关联 sys_user.id
     * <p>
     * 个人消息必填；广播消息（broadcastRole 非空）时为空。
     */
    private Long userId;

    /**
     * 消息类型：1-审核通过 2-审核拒绝 3-系统通知
     * <p>
     * 前端可据此展示不同图标 / 颜色：
     * <ul>
     *   <li>1 绿色成功</li>
     *   <li>2 红色警告</li>
     *   <li>3 蓝色普通</li>
     * </ul>
     */
    private Integer type;

    /**
     * 关联业务类型：1-卡面 2-对话版本 3-剧情（预留）
     * <p>
     * 与 {@link #targetId} 组合后，前端可精确跳转到对应详情页。
     */
    private Integer targetType;

    /**
     * 关联业务主键
     * <p>
     * targetType = 1 时为 card.id；<br>
     * targetType = 2 时为 dialogue_version.id；<br>
     * targetType = 3 时为 story.id。
     */
    private Long targetId;

    /**
     * 消息标题
     * <p>
     * 例如："卡面审核通过"、"对话版本审核未通过"、"系统公告"
     */
    private String title;

    /**
     * 消息正文（生成时的快照）
     * <p>
     * 例如：
     * <ul>
     *   <li>"您提交的卡面「千[Photo Collection -润-]」已通过审核"</li>
     *   <li>"您提交的对话版本未通过审核，原因：图片不清晰，请重新上传"</li>
     * </ul>
     * 注意：content 是消息生成时拼好的文本，不会随业务数据修改而改变，
     * 这样即使原业务记录被删除 / 审核备注被清空，用户仍能看到完整的历史消息。
     */
    private String content;

    /**
     * 是否已读：0-未读 1-已读
     * <p>
     * 使用 TINYINT 而非 BOOLEAN，与项目内其他状态字段（status 等）保持一致。
     */
    private Integer isRead;

    /**
     * 广播角色：NULL-个人消息 1-广播给管理员
     * <p>
     * 为 NULL 时按 user_id 定向投递给某个用户；
     * 非空时 user_id 为空，该消息属于某个角色群体，
     * 所有属于该角色的用户共享同一条消息和同一个已读状态。
     */
    private Integer broadcastRole;

    /**
     * 首个已读用户ID（仅广播消息使用）
     * <p>
     * 因为广播消息的 is_read 是"群体共享"的，
     * 记录谁第一个点开有助于审计和"已由 XXX 处理"的展示。
     */
    private Long readByUserId;

    /**
     * 阅读时间
     * <p>
     * 首次标记为已读时写入，可用于"消息停留时长"、"消息响应速度"等统计。
     */
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
