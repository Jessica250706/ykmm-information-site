package com.xq.service;

import com.xq.dto.UserEditDTO;
import com.xq.dto.UserPageQueryDTO;
import com.xq.entity.SysUser;
import com.xq.result.PageResult;

public interface UserService {

    /**
     * 分页查询用户
     */
    PageResult pageQuery(UserPageQueryDTO query);

    /**
     * 用户详情
     */
    SysUser getById(Long id);

    /**
     * 编辑用户
     */
    void update(Long id, UserEditDTO dto);

    /**
     * 删除用户
     */
    void delete(Long id);

    /**
     * 启用/禁用
     */
    void updateStatus(Long id, Integer status);
}