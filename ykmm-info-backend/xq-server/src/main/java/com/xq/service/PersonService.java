package com.xq.service;

import com.xq.dto.PersonDTO;
import com.xq.dto.PersonPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.PersonOptionVO;
import com.xq.vo.PersonVO;

import java.util.List;

public interface PersonService {

    /**
     * 分页查询
     */
    PageResult<PersonVO> pageQuery(PersonPageQueryDTO query);

    /**
     * 查询全部
     */
    List<PersonOptionVO> listOptions();

    /**
     * 详情
     */
    PersonVO getById(Long id);

    /**
     * 新增
     */
    Long create(PersonDTO dto);

    /**
     * 编辑
     */
    void update(Long id, PersonDTO dto);

    /**
     * 删除
     */
    void delete(Long id);
}
