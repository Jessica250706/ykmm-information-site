package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.dto.UserEditDTO;
import com.xq.dto.UserPageQueryDTO;
import com.xq.entity.SysUser;
import com.xq.mapper.UserMapper;
import com.xq.result.PageResult;
import com.xq.service.UserService;
import com.xq.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
    public void update(Long id, UserEditDTO dto) {
        SysUser exist = userMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }

        // 邮箱变更时，做唯一性校验
        if (dto.getEmail() != null && !dto.getEmail().equals(exist.getEmail())) {
            SysUser byEmail = userMapper.getByEmail(dto.getEmail());
            if (byEmail != null && !byEmail.getId().equals(id)) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        SysUser update = new SysUser();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        userMapper.update(update);
        log.info("编辑用户成功，id={}", id);
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
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("状态值不合法");
        }
        SysUser exist = userMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.updateStatus(id, status);
        log.info("更新用户状态成功，id={}, status={}", id, status);
    }
}
