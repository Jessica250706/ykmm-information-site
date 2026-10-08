package com.xq.mapper;

import com.xq.entity.CardRtv;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面 RTV Mapper
 */
@Mapper
public interface CardRtvMapper {
    /**
     * 根据卡面ID查询 RTV 列表
     */
    List<CardRtv> listByCardId(@Param("cardId") Long cardId);

    /**
     * 根据 id 查询
     */
    CardRtv getById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(CardRtv rtv);

    /**
     * 更新
     */
    int update(CardRtv rtv);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除卡面下的全部 RTV
     */
    int deleteByCardId(@Param("cardId") Long cardId);
}
