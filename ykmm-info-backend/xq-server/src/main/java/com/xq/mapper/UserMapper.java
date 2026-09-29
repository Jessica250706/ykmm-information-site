package com.xq.mapper;

import com.github.pagehelper.Page;
import com.xq.dto.UserPageQueryDTO;
import com.xq.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 分页查询用户
     */
    Page<SysUser> pageQuery(UserPageQueryDTO query);

    /**
     * 统计总数
     */
    long countByQuery(UserPageQueryDTO query);

    /**
     * 根据 id 查询
     */
    SysUser getById(@Param("id") Long id);

    /**
     * 根据邮箱查询，用于唯一性校验
     */
    SysUser getByEmail(@Param("email") String email);

    /**
     * 更新用户信息（只更新非空字段）
     */
    int update(SysUser user);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 物理删除
     */
    int deleteById(@Param("id") Long id);
}
