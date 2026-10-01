package com.xq.mapper;

import com.xq.entity.Agency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 经纪公司 Mapper
 */
@Mapper
public interface AgencyMapper {

    /**
     * 查询全部经纪公司
     *
     * @return 经纪公司列表
     */
    List<Agency> listAll();

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 经纪公司
     */
    Agency getById(@Param("id") Long id);

    /**
     * 根据名称查询，用于唯一性校验
     *
     * @param name 公司名
     * @return 经纪公司
     */
    Agency getByName(@Param("name") String name);

    /**
     * 新增
     *
     * @param agency 经纪公司
     * @return 影响行数
     */
    int insert(Agency agency);

    /**
     * 更新
     *
     * @param agency 经纪公司
     * @return 影响行数
     */
    int update(Agency agency);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计该公司的偶像团体数量
     *
     * @param agencyId 公司ID
     * @return 数量
     */
    int countGroupByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * 统计该公司关联的人物数量
     *
     * @param agencyId 公司ID
     * @return 数量
     */
    int countPersonByAgencyId(@Param("agencyId") Long agencyId);
}