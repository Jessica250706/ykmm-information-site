package com.xq.controller.admin;

import com.xq.dto.IdolGroupDTO;
import com.xq.result.Result;
import com.xq.service.IdolGroupService;
import com.xq.vo.IdolGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 偶像团体管理
 */
@RestController("AdminIdolGroupController")
@RequestMapping("/admin/idol-groups")
@Slf4j
public class IdolGroupController {

    @Autowired
    private IdolGroupService idolGroupService;

    /**
     * 查询全部偶像团体
     *
     * @return 偶像团体列表
     */
    @GetMapping
    public Result<List<IdolGroupVO>> listAll() {
        return Result.success(idolGroupService.listAll());
    }

    /**
     * 查询偶像团体详情
     *
     * @param id 主键
     * @return 偶像团体详情
     */
    @GetMapping("/{id}")
    public Result<IdolGroupVO> detail(@PathVariable Long id) {
        return Result.success(idolGroupService.getById(id));
    }

    /**
     * 新增偶像团体
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @PostMapping
    public Result<Long> create(@RequestBody IdolGroupDTO dto) {
        return Result.success(idolGroupService.create(dto));
    }

    /**
     * 编辑偶像团体
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody IdolGroupDTO dto) {
        idolGroupService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除偶像团体
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        idolGroupService.delete(id);
        return Result.success();
    }
}
