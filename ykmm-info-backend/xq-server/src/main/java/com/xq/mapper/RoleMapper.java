package com.xq.mapper;

import com.xq.dto.RolePageQueryDTO;
import com.xq.dto.StoryCategorySimpleDTO;
import com.xq.entity.Role;
import com.xq.vo.StoryCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 角色列表
     */
    List<Role> pageQuery(@Param("query") RolePageQueryDTO query);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 角色
     */
    Role getById(@Param("id") Long id);

    /**
     * 根据角色名精确查询
     *
     * @param name 角色名
     * @return 角色
     */
    Role getByName(@Param("name") String name);

    /**
     * 根据人物中文名查询角色
     *
     * @param nameCn 人物中文名
     * @return 角色
     */
    Role getByPersonNameCn(@Param("nameCn") String nameCn);

    /**
     * 新增
     *
     * @param role 角色
     * @return 影响行数
     */
    int insert(Role role);

    /**
     * 更新
     *
     * @param role 角色
     * @return 影响行数
     */
    int update(Role role);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计该人物下的角色数量
     *
     * @param personId 人物ID
     * @return 数量
     */
    int countByPersonId(@Param("personId") Long personId);

    /**
     * 查询角色关联的分类根节点ID列表
     *
     * @param roleId 角色ID
     * @return 分类ID列表
     */
    List<Long> listCategoryIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的分类关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteCategoryRel(@Param("roleId") Long roleId);

    /**
     * 批量插入角色-分类关系
     *
     * @param roleId          角色ID
     * @param categoryIds 分类ID列表
     * @return 影响行数
     */
    int insertCategoryRel(@Param("roleId") Long roleId,
                          @Param("categoryIds") List<Long> categoryIds);

    /**
     * 查询角色关联的分类信息
     *
     * @param roleId 角色ID
     * @return 分类列表
     */
    List<StoryCategorySimpleDTO> listCategoriesByRoleId(@Param("roleId") Long roleId);
}
