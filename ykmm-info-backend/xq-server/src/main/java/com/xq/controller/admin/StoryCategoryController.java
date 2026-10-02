package com.xq.controller.admin;

import com.xq.dto.StoryCategoryDTO;
import com.xq.dto.StoryCategoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StoryCategoryService;
import com.xq.vo.StoryCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 剧情分类管理
 */
@RestController("AdminStoryCategoryController")
@RequestMapping("/admin/story-category")
@Slf4j
public class StoryCategoryController {

    @Autowired
    private StoryCategoryService storyCategoryService;

    /**
     * 查询所有剧情分类树（不分页）
     *
     * @param categoryType 分类类型，可选
     * @return 分类树
     */
    @GetMapping("/tree")
    public Result<List<StoryCategoryVO>> listTree(@RequestParam(required = false) Integer categoryType) {
        return Result.success(storyCategoryService.listTree(categoryType));
    }

    /**
     * 分页查询剧情分类树
     *
     * @param query 查询条件
     * @return 分页后的分类树
     */
    @GetMapping("/page")
    public Result<PageResult<StoryCategoryVO>> pageTree(StoryCategoryPageQueryDTO query) {
        return Result.success(storyCategoryService.pageTree(query));
    }

    /**
     * 查询分类详情
     *
     * @param id 主键
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public Result<StoryCategoryVO> detail(@PathVariable Long id) {
        return Result.success(storyCategoryService.getById(id));
    }

    /**
     * 新增分类
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @PostMapping
    public Result<Long> create(@RequestBody StoryCategoryDTO dto) {
        return Result.success(storyCategoryService.create(dto));
    }

    /**
     * 编辑分类
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody StoryCategoryDTO dto) {
        storyCategoryService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除分类
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        storyCategoryService.delete(id);
        return Result.success();
    }
}
