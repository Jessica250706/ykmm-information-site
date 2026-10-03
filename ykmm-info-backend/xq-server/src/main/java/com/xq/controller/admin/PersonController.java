package com.xq.controller.admin;

import com.xq.dto.PersonDTO;
import com.xq.dto.PersonPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.PersonService;
import com.xq.vo.PersonOptionVO;
import com.xq.vo.PersonVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人物管理
 */
@RestController("AdminPersonController")
@RequestMapping("/admin/person")
@Slf4j
public class PersonController {

    @Autowired
    private PersonService personService;

    /**
     * 分页查询人物
     */
    @GetMapping
    public Result<PageResult<PersonVO>> page(PersonPageQueryDTO query) {
        log.info("管理端查询人物列表：{}", query);
        return Result.success(personService.pageQuery(query));
    }

    /**
     * 查询所有人物
     */
    @GetMapping("/options")
    public Result<List<PersonOptionVO>> options() {
        return Result.success(personService.listOptions());
    }

    /**
     * 查询人物详情
     */
    @GetMapping("/{id}")
    public Result<PersonVO> detail(@PathVariable Long id) {
        return Result.success(personService.getById(id));
    }

    /**
     * 新增人物
     */
    @PostMapping
    public Result<Long> create(@RequestBody PersonDTO dto) {
        return Result.success(personService.create(dto));
    }

    /**
     * 编辑人物
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody PersonDTO dto) {
        personService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除人物
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return Result.success();
    }
}