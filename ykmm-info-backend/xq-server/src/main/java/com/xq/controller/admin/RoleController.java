package com.xq.controller.admin;

import com.xq.dto.RoleDTO;
import com.xq.dto.RolePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.RoleService;
import com.xq.vo.RoleGroupVO;
import com.xq.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理
 */
@RestController("AdminRoleController")
@RequestMapping("/admin/role")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<RoleVO>> page(RolePageQueryDTO query) {
        log.info("管理端查询角色列表：{}", query);
        return Result.success(roleService.pageQuery(query));
    }

    /**
     * 查询角色详情
     *
     * @param id 主键
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 新增角色
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @PostMapping
    public Result<Long> create(@RequestBody RoleDTO dto) {
        return Result.success(roleService.create(dto));
    }

    /**
     * 编辑角色
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody RoleDTO dto) {
        roleService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除角色
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

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
