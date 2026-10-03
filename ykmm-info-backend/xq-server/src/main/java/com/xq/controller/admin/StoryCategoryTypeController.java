package com.xq.controller.admin;

import com.xq.dto.StoryCategoryTypeDTO;
import com.xq.result.Result;
import com.xq.service.StoryCategoryTypeService;
import com.xq.vo.StoryCategoryTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 剧情分类类型管理
 */
@RestController("AdminStoryCategoryTypeController")
@RequestMapping("/admin/story-category-type")
@Slf4j
public class StoryCategoryTypeController {

    @Autowired
    private StoryCategoryTypeService storyCategoryTypeService;

    /**
     * 查询全部
     *
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<StoryCategoryTypeVO>> list() {
        return Result.success(storyCategoryTypeService.listAll());
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 详情
     */
    @GetMapping("/{id}")
    public Result<StoryCategoryTypeVO> detail(@PathVariable Integer id) {
        return Result.success(storyCategoryTypeService.getById(id));
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id,
                               @RequestBody StoryCategoryTypeDTO dto) {
        storyCategoryTypeService.update(id, dto);
        return Result.success();
    }
}
