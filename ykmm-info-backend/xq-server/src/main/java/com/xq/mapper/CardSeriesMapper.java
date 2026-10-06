package com.xq.mapper;

import com.xq.entity.CardSeries;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面系列 Mapper
 */
@Mapper
public interface CardSeriesMapper {
    /** 查询全部 */
    List<CardSeries> listAll();
    /** 根据 id 查询 */
    CardSeries getById(@Param("id") Long id);
    /** 根据名称模糊查询 */
    List<CardSeries> searchByName(@Param("keyword") String keyword);
    /** 新增 */
    int insert(CardSeries series);
    /** 更新 */
    int update(CardSeries series);
    /** 删除 */
    int deleteById(@Param("id") Long id);
    /** 统计关联卡面数 */
    int countCards(@Param("seriesId") Long seriesId);
}
