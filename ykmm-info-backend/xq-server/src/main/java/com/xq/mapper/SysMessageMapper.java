package com.xq.mapper;

import com.xq.dto.SysMessagePageQueryDTO;
import com.xq.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统消息 Mapper
 */
@Mapper
public interface SysMessageMapper {

    void insert(SysMessage message);

    SysMessage getById(@Param("id") Long id);

    /**
     * 分页查询消息
     * <p>
     * 语义由 currentId + isAdmin 决定：
     * <ul>
     *   <li>普通用户：只看 user_id = currentId 的个人消息</li>
     *   <li>管理员：自己的个人消息 + 全部广播消息</li>
     *   <li>query.userId 指定时：精确查该用户的个人消息</li>
     * </ul>
     */
    List<SysMessage> pageQuery(@Param("query") SysMessagePageQueryDTO query,
                               @Param("currentId") Long currentId,
                               @Param("isAdmin") boolean isAdmin);

    /**
     * 统计未读数
     * <p>
     * 普通用户：自己的未读个人消息；
     * 管理员：自己的未读个人消息 + 全部未读广播。
     */
    long countUnread(@Param("userId") Long userId,
                     @Param("isAdmin") boolean isAdmin);

    /**
     * 标记个人消息为已读
     * <p>只能标记自己的，userId 非空且必须匹配 user_id。
     */
    int markPersonalRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 标记广播消息为已读
     * <p>仅 broadcast_role 非空的消息生效；任一管理员操作即全群体已读。
     */
    int markBroadcastRead(@Param("id") Long id, @Param("readerId") Long readerId);

    /**
     * 批量标记"我的"个人消息为已读
     */
    int markAllPersonalRead(@Param("userId") Long userId);

    /**
     * 批量标记全部广播消息为已读（管理员用）
     */
    int markAllBroadcastRead(@Param("readerId") Long readerId);
}
