package com.xq.mapper;

import com.xq.entity.StoryCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 剧情分类 Mapper
 */
@Mapper
public interface StoryCategoryMapper {

    /**
     * 查询全部剧情分类
     *
     * @return 分类列表
     */
    List<StoryCategory> listAll();

    /**
     * 按分类类型查询
     *
     * @param categoryType 分类类型
     * @return 分类列表
     */
    List<StoryCategory> listByType(@Param("categoryType") Integer categoryType);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 分类
     */
    StoryCategory getById(@Param("id") Long id);

    /**
     * 统计子分类数量
     *
     * @param parentId 父分类ID
     * @return 数量
     */
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 统计分类下剧情数量
     *
     * @param categoryId 分类ID
     * @return 数量
     */
    int countStoryByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 新增
     *
     * @param storyCategory 分类
     * @return 影响行数
     */
    int insert(StoryCategory storyCategory);

    /**
     * 更新
     *
     * @param storyCategory 分类
     * @return 影响行数
     */
    int update(StoryCategory storyCategory);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询根节点（PageHelper 分页）
     */
    List<StoryCategory> listRoots(@Param("categoryType") Integer categoryType);

    /**
     * 根据根节点 ID 列表查询其所有子孙
     */
    List<StoryCategory> listDescendants(@Param("rootIds") List<Long> rootIds);

    /**
     * 分页某父节点的直接子节点
     */
    List<StoryCategory> listChildren(@Param("parentId") Long parentId,
                                     @Param("categoryType") Integer categoryType);
}
