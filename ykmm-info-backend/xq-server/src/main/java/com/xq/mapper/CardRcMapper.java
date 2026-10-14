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
    /**
     * 根据卡面ID查询 RC 列表
     */
    List<CardRcVO> listVOByCardId(@Param("cardId") Long cardId);

    /**
     * 根据 id 查询实体
     */
    CardRc getById(@Param("id") Long id);

    /**
     * 根据 id 查询 VO
     */
    CardRcVO getVOById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(CardRc rc);

    /**
     * 更新
     */
    int update(CardRc rc);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除卡面下的全部 RC
     */
    int deleteByCardId(@Param("cardId") Long cardId);

    /**
     * 根据卡面ID查询全部 RC 主键
     * <p>用于级联删除卡面时，先查出下属所有 RC 主键</p>
     *
     * @param cardId 卡面ID
     * @return RC 主键列表
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
