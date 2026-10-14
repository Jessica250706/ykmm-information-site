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
}
