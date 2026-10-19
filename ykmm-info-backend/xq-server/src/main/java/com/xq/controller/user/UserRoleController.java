package com.xq.controller.user;

import com.xq.result.Result;
import com.xq.service.RoleService;
import com.xq.vo.RoleGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理
 */
@RestController("UserRoleController")
@RequestMapping("/user/role")
@Slf4j
public class UserRoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 查询全部角色（按人物分组）
     *
     * @return 分组列表
     */
    @GetMapping("/all-grouped")
    public Result<List<RoleGroupVO>> listAllGrouped() {
        return Result.success(roleService.listAllGroupedByPerson());
    }
}
