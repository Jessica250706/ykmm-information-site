package com.xq.mapper;

import com.xq.dto.CardCategoryPageQueryDTO;
import com.xq.entity.CardCategory;
import com.xq.vo.CardCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡面类别 Mapper
 */
@Mapper
public interface CardCategoryMapper {

    /**
     * 分页查询类别（附带每个类别关联的卡面数量）
     *
     * @param query 查询条件（keyword）
     * @return 类别 VO 列表（含 cardCount）
     */
    List<CardCategoryVO> pageQuery(CardCategoryPageQueryDTO query);

    /**
     * 查询全部类别（按 sort 升序，id 升序），供下拉使用
     */
    List<CardCategory> listAll();

    /**
     * 根据 id 查询
     */
    CardCategory getById(@Param("id") Integer id);

    /**
     * 判断同名类别是否存在
     *
     * @param name      类别名
     * @param excludeId 排除的 id（编辑时排除自己）；新增传 null
     */
    int countByName(@Param("name") String name, @Param("excludeId") Integer excludeId);

    /**
     * 统计引用该类别 id 的卡面数量
     */
    int countCardsByCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * 新增
     */
    int insert(CardCategory category);

    /**
     * 更新（部分更新，null 字段不改）
     */
    int update(CardCategory category);

    /**
     * 删除
     */
    int deleteById(@Param("id") Integer id);
}
