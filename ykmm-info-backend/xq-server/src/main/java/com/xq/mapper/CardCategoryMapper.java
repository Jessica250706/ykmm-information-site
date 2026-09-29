package com.xq.mapper;

import com.xq.entity.CardCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CardCategoryMapper {

    /**
     * 查询全部卡面类别
     */
    List<CardCategory> listAll();
}
