package com.xq.mapper;

import com.xq.entity.CardImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面图片 Mapper
 */
@Mapper
public interface CardImageMapper {
    /** 根据卡面ID查询图片 */
    List<CardImage> listByCardId(@Param("cardId") Long cardId);
    /** 新增 */
    int insert(CardImage image);
    /** 批量新增 */
    int insertBatch(@Param("list") List<CardImage> list);
    /** 根据卡面ID删除 */
    int deleteByCardId(@Param("cardId") Long cardId);
}
