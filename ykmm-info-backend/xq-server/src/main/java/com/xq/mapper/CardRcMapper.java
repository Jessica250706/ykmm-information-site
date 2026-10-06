package com.xq.mapper;

import com.xq.entity.CardRc;
import com.xq.vo.CardRcVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面 RC Mapper
 */
@Mapper
public interface CardRcMapper {
    /** 根据卡面ID查询 RC 列表 */
    List<CardRcVO> listVOByCardId(@Param("cardId") Long cardId);
    /** 根据 id 查询实体 */
    CardRc getById(@Param("id") Long id);
    /** 根据 id 查询 VO */
    CardRcVO getVOById(@Param("id") Long id);
    /** 新增 */
    int insert(CardRc rc);
    /** 更新 */
    int update(CardRc rc);
    /** 删除 */
    int deleteById(@Param("id") Long id);
    /** 删除卡面下的全部 RC */
    int deleteByCardId(@Param("cardId") Long cardId);
}
