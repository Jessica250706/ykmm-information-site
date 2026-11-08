package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.constant.QuizPassedConstant;
import com.xq.constant.UserStatusConstant;
import com.xq.constant.RedisConstant;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.UserEditDTO;
import com.xq.dto.UserPageQueryDTO;
import com.xq.entity.SysQuizConfig;
import com.xq.entity.SysUser;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.mapper.QuizConfigMapper;
import com.xq.mapper.UserMapper;
import com.xq.result.PageResult;
import com.xq.service.UserService;
import com.xq.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QuizConfigMapper quizConfigMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<UserInfo> pageQuery(UserPageQueryDTO query) {
        // PageHelper 分页
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<SysUser> page = userMapper.pageQuery(query);

        // SysUser → UserInfo
        List<UserInfo> list = page.getResult().stream()
                .map(UserInfo::from)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), list);
    }

    @Override
    public UserInfo getById(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        SysUser user = userMapper.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return UserInfo.from(user);
    }

    @Override
    @Transactional
    public UserInfo update(Long id, UserEditDTO dto) {
        // 1. 登录校验
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }

        // 2. 权限校验：普通用户只能改自己，管理员全放行
        boolean admin = RoleContext.isAdmin();
        if (!admin && !currentId.equals(id)) {
            throw new ForbiddenException("只能修改自己的信息");
        }

        SysUser exist = userMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 普通用户不允许修改 status（禁用/启用是管理员权限）
        if (!admin) {
            dto.setStatus(null);
        }

        if (dto.getNickname() == null
                && dto.getAvatar() == null
                && dto.getStatus() == null) {
            log.info("用户编辑未产生实际变更，直接返回：id={}", id);
            return getById(id);
        }

        SysUser update = new SysUser();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        userMapper.update(update);

        log.info("编辑用户成功，id={}, operatorId={}, admin={}", id, currentId, admin);

        // 5. 返回最新用户信息
        return getById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser exist = userMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(id);
        log.info("删除用户成功，id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != UserStatusConstant.DISABLED && status != UserStatusConstant.ENABLED)) {
            throw new RuntimeException("状态值不合法");
        }
        SysUser exist = userMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.updateStatus(id, status);
        log.info("更新用户状态成功，id={}, status={}", id, status);
    }

    @Override
    public boolean canUseUserFeature(Long userId) {
        if (userId == null) return false;

        // 1. 答题开关（Redis 缓存，避免每次查数据库）
        if (!isQuizEnabled()) {
            return true;   // 答题未开启，全放行
        }

        // 2. 用户是否通过答题（Redis 缓存）
        String cacheKey = RedisConstant.QUIZ_PASSED_KEY_PREFIX + userId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return "1".equals(cached);
            }
        } catch (Exception e) {
            // Redis 挂了回源数据库，保证可用性
            log.warn("Redis 查询失败，回源数据库：userId={}", userId, e);
            return queryFromDb(userId);
        }

        // 3. 缓存未命中，回源数据库并写缓存
        boolean result = queryFromDb(userId);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, result ? "1" : "0", RedisConstant.QUIZ_PASSED_TTL);
        } catch (Exception e) {
            log.warn("Redis 写缓存失败：userId={}", userId, e);
        }
        return result;
    }

    /** 答题开关：带 Redis 缓存 */
    private boolean isQuizEnabled() {
        try {
            String cached = stringRedisTemplate.opsForValue().get(RedisConstant.QUIZ_ENABLED_KEY);
            if (cached != null) return "1".equals(cached);
        } catch (Exception e) {
            log.warn("Redis 查询答题开关失败，回源数据库", e);
        }

        SysQuizConfig config = quizConfigMapper.get();
        boolean enabled = config != null
                && config.getEnabled() != null
                && config.getEnabled() == 1;
        try {
            stringRedisTemplate.opsForValue().set(RedisConstant.QUIZ_ENABLED_KEY, enabled ? "1" : "0", RedisConstant.QUIZ_ENABLED_TTL);
        } catch (Exception e) {
            log.warn("Redis 写答题开关缓存失败", e);
        }
        return enabled;
    }

    /** 纯数据库查询（回源用） */
    private boolean queryFromDb(Long userId) {
        SysUser user = userMapper.getById(userId);
        if (user == null) return false;
        return QuizPassedConstant.PASSED == (user.getQuizPassed() == null ? 0 : user.getQuizPassed());
    }

    @Override
    public void refreshQuizPassedCache(Long userId) {
        if (userId == null) return;
        try {
            stringRedisTemplate.delete(RedisConstant.QUIZ_PASSED_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("清除答题缓存失败：userId={}", userId, e);
        }
    }

    @Override
    public void refreshQuizEnabledCache() {
        try {
            stringRedisTemplate.delete(RedisConstant.QUIZ_ENABLED_KEY);
        } catch (Exception e) {
            log.warn("清除答题开关缓存失败", e);
        }
    }
}
