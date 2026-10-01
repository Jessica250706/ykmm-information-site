package com.xq.controller.admin;

import com.xq.dto.AgencyDTO;
import com.xq.result.Result;
import com.xq.service.AgencyService;
import com.xq.vo.AgencyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 经纪公司管理
 */
@RestController("AdminAgencyController")
@RequestMapping("/admin/agencies")
@Slf4j
public class AgencyController {

    @Autowired
    private AgencyService agencyService;

    /**
     * 查询全部经纪公司
     *
     * @return 经纪公司列表
     */
    @GetMapping
    public Result<List<AgencyVO>> listAll() {
        return Result.success(agencyService.listAll());
    }

    /**
     * 查询经纪公司详情
     *
     * @param id 主键
     * @return 经纪公司详情
     */
    @GetMapping("/{id}")
    public Result<AgencyVO> detail(@PathVariable Long id) {
        return Result.success(agencyService.getById(id));
    }

    /**
     * 新增经纪公司
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @PostMapping
    public Result<Long> create(@RequestBody AgencyDTO dto) {
        return Result.success(agencyService.create(dto));
    }

    /**
     * 编辑经纪公司
     *
     * @param id  主键
     * @param dto 编辑参数
     * @return 统一返回
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody AgencyDTO dto) {
        agencyService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除经纪公司
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        agencyService.delete(id);
        return Result.success();
    }
}
