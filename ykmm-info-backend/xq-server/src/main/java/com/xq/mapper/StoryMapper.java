package com.xq.mapper;

import com.xq.dto.StoryPageQueryDTO;
import com.xq.entity.Story;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 剧情 Mapper
 */
@Mapper
public interface StoryMapper {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 剧情列表
     */
    List<Story> pageQuery(@Param("query") StoryPageQueryDTO query);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 剧情
     */
    Story getById(@Param("id") Long id);

    /**
     * 新增
     *
     * @param story 剧情
     * @return 影响行数
     */
    int insert(Story story);

    /**
     * 更新
     *
     * @param story 剧情
     * @return 影响行数
     */
    int update(Story story);

    /**
     * 更新审核状态
     *
     * @param story 剧情
     * @return 影响行数
     */
    int updateStatus(Story story);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计分类下剧情数量
     *
     * @param categoryId 分类ID
     * @return 数量
     */
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID查询剧情列表
     *
     * @param categoryId 分类ID
     * @return 剧情列表
     */
    List<Story> listByCategoryId(@Param("categoryId") Long categoryId);
}
