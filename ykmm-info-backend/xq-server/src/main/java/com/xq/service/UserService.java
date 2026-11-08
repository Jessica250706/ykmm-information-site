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

    /**
     * 判断用户是否可以使用需要答题门槛的接口。
     * <p>
     * 判定逻辑：
     * <ol>
     *   <li>答题功能未开启 → 返回 true（无需答题）</li>
     *   <li>用户已通过答题 → 返回 true</li>
     *   <li>用户不存在或未通过 → 返回 false</li>
     * </ol>
     * <p>
     * 说明：管理员不经过此方法判断，由调用方（拦截器）独立豁免。
     *
     * @param userId 用户ID
     * @return true 表示可以使用需要答题门槛的接口
     */
    boolean canUseUserFeature(Long userId);

    /**
     * 清除用户的答题通过状态缓存。
     * <p>答题通过 / 被管理员重置时调用。
     */
    void refreshQuizPassedCache(Long userId);

    /**
     * 清除答题开关缓存。
     * <p>管理员修改答题配置时调用。
     */
    void refreshQuizEnabledCache();
}