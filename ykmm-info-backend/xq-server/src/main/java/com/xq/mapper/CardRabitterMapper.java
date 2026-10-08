package com.xq.mapper;

import com.xq.entity.CardRabitter;
import com.xq.vo.CardRabitterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面 Rabitter Mapper
 */
@Mapper
public interface CardRabitterMapper {
    /**
     * 根据卡面ID查询 Rabitter 列表
     */
    List<CardRabitter> listByCardId(@Param("cardId") Long cardId);

    /**
     * 根据 id 查询实体
     */
    CardRabitter getById(@Param("id") Long id);

    /**
     * 根据 id 查询 VO
     */
    CardRabitterVO getVOById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(CardRabitter rabitter);

    /**
     * 更新
     */
    int update(CardRabitter rabitter);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除卡面下的全部 Rabitter
     */
    int deleteByCardId(@Param("cardId") Long cardId);
}
