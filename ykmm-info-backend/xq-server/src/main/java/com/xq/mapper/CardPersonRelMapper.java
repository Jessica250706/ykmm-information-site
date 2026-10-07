package com.xq.mapper;

import com.xq.vo.CardPersonVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面-人物关系 Mapper
 */
@Mapper
public interface CardPersonRelMapper {

    /**
     * 查询卡面关联的人物
     *
     * @param cardId 卡面ID
     * @return 人物列表
     */
    List<CardPersonVO> listPersonsByCardId(@Param("cardId") Long cardId);

    /**
     * 根据卡面ID批量查询关联人物（用于列表分页组装，避免 N+1）
     *
     * @param cardIds 卡面ID列表
     * @return 人物列表，其中 CardPersonVO 需附带 cardId
     */
    List<CardPersonVO> listPersonsByCardIds(@Param("cardIds") List<Long> cardIds);

    /**
     * 批量新增
     *
     * @param cardId    卡面ID
     * @param personIds 人物ID列表
     * @return 影响行数
     */
    int insertBatch(@Param("cardId") Long cardId, @Param("personIds") List<Long> personIds);

    /**
     * 根据卡面ID删除
     *
     * @param cardId 卡面ID
     * @return 影响行数
     */
    int deleteByCardId(@Param("cardId") Long cardId);
}
