package com.xq.mapper;

import com.xq.dto.CardPageQueryDTO;
import com.xq.entity.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面 Mapper
 */
@Mapper
public interface CardMapper {
    /**
     * 分页查询
     *
     * @param query    查询条件
     * @param viewerId 访客用户ID：
     *                 - 游客：null（此时 query.status = 1）
     *                 - 用户：当前登录用户ID（此时 query.status 为 null）
     *                 - 管理员：null（此时 query.status 由前端决定，用于后台筛选）
     */
    List<Card> pageQuery(@Param("query") CardPageQueryDTO query,
                         @Param("viewerId") Long viewerId);

    /**
     * 根据 id 查询
     */
    Card getById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(Card card);

    /**
     * 更新
     */
    int update(Card card);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 判断同名卡面是否存在
     */
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 将卡面置为待审核，并清空上一次审核记录
     *
     * @param id 卡面ID
     */
    void markPending(@Param("id") Long id);

    /**
     * 分页查询指定用户创建的卡面（含所有状态）
     *
     * @param query     查询条件（keyword/seriesId/maxRarity/attribute/status 等）
     * @param creatorId 创建者用户ID
     */
    List<Card> pageQueryByCreator(@Param("query") CardPageQueryDTO query,
                                  @Param("creatorId") Long creatorId);
}
