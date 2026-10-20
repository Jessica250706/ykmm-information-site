package com.xq.service;

import com.xq.dto.UserEditDTO;
import com.xq.dto.UserPageQueryDTO;
import com.xq.entity.SysUser;
import com.xq.result.PageResult;
import com.xq.vo.UserInfo;

public interface UserService {

    /**
     * 分页查询用户
     */
    PageResult<UserInfo> pageQuery(UserPageQueryDTO query);

    /**
     * 用户详情
     */
    UserInfo getById(Long id);

    /**
     * 编辑用户信息。
     * <p>
     * 权限规则：
     * <ul>
     *   <li>普通用户：只能改自己；不允许改 status</li>
     *   <li>管理员：可改任意用户，含 status</li>
     * </ul>
     *
     * @param id  用户ID
     * @param dto 编辑参数
     * @return 更新后的用户信息
     */
    UserInfo update(Long id, UserEditDTO dto);

    /**
     * 删除用户
     */
    void delete(Long id);

    /**
     * 启用/禁用
     */
    void updateStatus(Long id, Integer status);
}