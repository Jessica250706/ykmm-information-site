package com.xq.mapper;

import com.xq.dto.NoticePageQueryDTO;
import com.xq.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告 Mapper
 */
@Mapper
public interface NoticeMapper {

    /**
     * 公共端分页：只返回「已发布 + 未过期 + 当前访问者可见」的公告
     *
     * @param query             查询条件
     * @param currentVisibility 当前访问者的可见级别
     */
    List<SysNotice> pageVisible(@Param("query") NoticePageQueryDTO query,
                                @Param("currentVisibility") int currentVisibility);

    /**
     * 管理端分页：返回全部公告（含草稿、已下线）
     */
    List<SysNotice> pageAdmin(@Param("query") NoticePageQueryDTO query);

    /**
     * 根据 ID 查询（不做可见性过滤，由 Service 决定是否放行）
     */
    SysNotice getById(@Param("id") Long id);

    int insert(SysNotice notice);

    int update(SysNotice notice);

    int deleteById(@Param("id") Long id);

    /**
     * 浏览量 +1
     */
    int incrViewCount(@Param("id") Long id);

    /**
     * 统计某分类下的公告数量（用于删除分类前的引用校验）
     */
    int countByCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * 把到期的"待发布"公告批量改为"已发布"
     * <p>
     * 条件：status = 3（待发布）且 publish_at 已到达
     * <p>
     * 不修改 publish_at 本身——保留用户设置的计划时间作为"发布时间"，
     * 让用户端看到的发布时间与用户预期一致。
     *
     * @return 本次真正被发布的条数（用于日志）
     */
    int publishScheduledNow();

    /**
     * 把已过期的「已发布」公告批量改为「已下线」
     * <p>
     * 条件：status = 1（已发布）且 expire_at 不为空且已到达
     * <p>
     * 不修改 expire_at 本身——保留用户设置的过期时间作为"原本计划的下架时间"，
     * 便于后台追溯"这条公告是什么时候到期的"。
     *
     * @return 本次真正被下架的条数（用于日志）
     */
    int expireOutdatedNow();
}
