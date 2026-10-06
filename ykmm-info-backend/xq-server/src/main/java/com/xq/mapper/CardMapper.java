package com.xq.mapper;

import com.xq.dto.CardPageQueryDTO;
import com.xq.entity.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面 Mapper
 */
@Mapper
public interface CardMapper {
    /** 分页查询 */
    List<Card> pageQuery(@Param("query") CardPageQueryDTO query);
    /** 根据 id 查询 */
    Card getById(@Param("id") Long id);
    /** 新增 */
    int insert(Card card);
    /** 更新 */
    int update(Card card);
    /** 删除 */
    int deleteById(@Param("id") Long id);
    /** 判断同名卡面是否存在 */
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);
}
