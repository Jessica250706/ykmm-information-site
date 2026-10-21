package com.xq.mapper;

import com.xq.dto.SysMessagePageQueryDTO;
import com.xq.entity.SysMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统消息 Mapper
 */
public interface SysMessageMapper {

    /**
     * 插入消息
     */
    void insert(SysMessage message);

    /**
     * 按主键查询
     */
    SysMessage getById(@Param("id") Long id);

    /**
     * 分页查询消息
     * <p>
     * 权限通过 viewerId 控制：
     * <ul>
     *   <li>viewerId != null：只看该用户的消息（普通用户）</li>
     *   <li>viewerId == null：不加 user_id 限制（管理员），
     *       但仍可通过 query.userId 精确筛选某个用户</li>
     * </ul>
     */
    List<SysMessage> pageQuery(@Param("query") SysMessagePageQueryDTO query,
                               @Param("viewerId") Long viewerId);

    /**
     * 统计未读消息数
     */
    long countUnread(@Param("userId") Long userId);

    /**
     * 将指定消息标记为已读
     *
     * @param id     消息ID
     * @param userId 归属用户ID；非 null 时作为越权防护条件
     */
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 将该用户的全部未读消息标记为已读
     */
    int markAllRead(@Param("userId") Long userId);
}
