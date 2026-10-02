package com.xq.service;

import com.xq.dto.RoleDTO;
import com.xq.dto.RolePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.RoleVO;

/**
 * 角色服务
 */
public interface RoleService {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RoleVO> pageQuery(RolePageQueryDTO query);

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 角色详情
     */
    RoleVO getById(Long id);

    /**
     * 新增角色
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    Long create(RoleDTO dto);

    /**
     * 编辑角色
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, RoleDTO dto);

    /**
     * 删除角色
     *
     * @param id 主键
     */
    void delete(Long id);
}
