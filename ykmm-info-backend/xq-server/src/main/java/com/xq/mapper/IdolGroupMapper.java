package com.xq.mapper;

import com.xq.entity.IdolGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 偶像团体 Mapper
 */
@Mapper
public interface IdolGroupMapper {

    /**
     * 查询全部偶像团体
     *
     * @return 偶像团体列表
     */
    List<IdolGroup> listAll();

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 偶像团体
     */
    IdolGroup getById(@Param("id") Long id);

    /**
     * 根据名称查询，用于唯一性校验
     *
     * @param name 团体名
     * @return 偶像团体
     */
    IdolGroup getByName(@Param("name") String name);

    /**
     * 新增
     *
     * @param idolGroup 偶像团体
     * @return 影响行数
     */
    int insert(IdolGroup idolGroup);

    /**
     * 更新
     *
     * @param idolGroup 偶像团体
     * @return 影响行数
     */
    int update(IdolGroup idolGroup);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计该团体关联的人物数量
     *
     * @param groupId 团体ID
     * @return 数量
     */
    int countPersonByGroupId(@Param("groupId") Long groupId);
}
