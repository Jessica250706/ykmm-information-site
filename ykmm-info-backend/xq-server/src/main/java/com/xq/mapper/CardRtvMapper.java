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

    /**
     * 根据卡面ID查询全部 RTV 主键
     * <p>用于级联删除卡面时，先查出下属所有 RTV 主键</p>
     *
     * @param cardId 卡面ID
     * @return RTV 主键列表
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
     * 判断指定用户能否操作该 RTV
     */
    Boolean canOperate(@Param("rtvId") Long rtvId,
                       @Param("userId") Long userId,
                       @Param("role") Integer role);
}
