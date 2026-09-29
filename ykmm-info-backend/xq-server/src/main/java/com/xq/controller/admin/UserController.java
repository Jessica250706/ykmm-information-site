package com.xq.controller.admin;

import com.xq.dto.UserEditDTO;
import com.xq.dto.UserPageQueryDTO;
import com.xq.dto.UserStatusDTO;
import com.xq.entity.SysUser;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理
 */
@RestController("AdminUserController")
@RequestMapping("/admin/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户列表（分页 + 条件查询）
     */
    @GetMapping
    public Result<PageResult> page(UserPageQueryDTO query) {
        log.info("管理端查询用户列表：{}", query);
        return Result.success(userService.pageQuery(query));
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 编辑用户
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody UserEditDTO dto) {
        userService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody UserStatusDTO dto) {
        userService.updateStatus(id, dto.getStatus());
        return Result.success();
    }
}
