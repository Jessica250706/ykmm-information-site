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
    /** 查询卡面关联的人物 */
    List<CardPersonVO> listPersonsByCardId(@Param("cardId") Long cardId);
    /** 批量新增 */
    int insertBatch(@Param("cardId") Long cardId, @Param("personIds") List<Long> personIds);
    /** 根据卡面ID删除 */
    int deleteByCardId(@Param("cardId") Long cardId);
}
