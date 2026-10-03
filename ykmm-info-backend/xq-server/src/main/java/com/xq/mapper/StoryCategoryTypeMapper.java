package com.xq.mapper;

import com.xq.entity.StoryCategoryType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 剧情分类类型 Mapper
 */
@Mapper
public interface StoryCategoryTypeMapper {

    /**
     * 查询全部（按 sort 排序）
     *
     * @return 列表
     */
    List<StoryCategoryType> listAll();

    /**
     * 按主键查询
     *
     * @param id 主键
     * @return 分类类型
     */
    StoryCategoryType getById(@Param("id") Integer id);

    /**
     * 更新（仅名称/描述/颜色/排序）
     *
     * @param type 待更新对象
     * @return 影响行数
     */
    int update(StoryCategoryType type);
}
