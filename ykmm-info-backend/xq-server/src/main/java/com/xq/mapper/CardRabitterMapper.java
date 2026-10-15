package com.xq.mapper;

import com.xq.entity.CardRabitter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面 Rabitter Mapper
 */
@Mapper
public interface CardRabitterMapper {

    /**
     * 查询卡面下的 Rabitter 列表
     *
     * @param cardId 卡面ID
     * @return Rabitter 列表
     */
    List<CardRabitter> listByCardId(@Param("cardId") Long cardId);

    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return Rabitter 实体
     */
    CardRabitter getById(@Param("id") Long id);

    /**
     * 新增
     *
     * @param rabiter Rabitter 实体
     * @return 影响行数
     */
    int insert(CardRabitter rabiter);

    /**
     * 更新
     *
     * @param rabiter Rabitter 实体
     * @return 影响行数
     */
    int update(CardRabitter rabiter);

    /**
     * 根据主键删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据卡面ID删除
     *
     * @param cardId 卡面ID
     * @return 影响行数
     */
    int deleteByCardId(@Param("cardId") Long cardId);

    /**
     * 根据卡面ID查询全部 Rabitter 主键
     * <p>用于级联删除卡面时，先查出下属所有 Rabitter 主键</p>
     *
     * @param cardId 卡面ID
     * @return Rabitter 主键列表
     */
    List<Long> listIdsByCardId(@Param("cardId") Long cardId);

    /**
     * 根据主键列表批量删除
     *
     * @param ids 主键列表（不能为空）
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 判断指定用户能否操作该 Rabitter
     * <p>
     * - 游客（userId / role 为 null）返回 false
     * - 管理员（role = 1）返回 true
     * - 普通用户：creator_id == userId 返回 true，否则 false
     *
     * @param rabitterId Rabitter 主键
     * @param userId     当前登录用户ID，游客传 null
     * @param role       当前用户角色，游客传 null
     * @return 能否操作
     */
    Boolean canOperate(@Param("rabitterId") Long rabitterId,
                       @Param("userId") Long userId,
                       @Param("role") Integer role);
}
