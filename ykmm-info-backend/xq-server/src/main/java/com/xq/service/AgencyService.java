package com.xq.service;

import com.xq.dto.AgencyDTO;
import com.xq.vo.AgencyVO;

import java.util.List;

/**
 * 经纪公司服务
 */
public interface AgencyService {

    /**
     * 查询全部经纪公司
     *
     * @return 经纪公司列表
     */
    List<AgencyVO> listAll();

    /**
     * 根据 id 查询详情
     *
     * @param id 主键
     * @return 经纪公司详情
     */
    AgencyVO getById(Long id);

    /**
     * 新增经纪公司
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    Long create(AgencyDTO dto);

    /**
     * 编辑经纪公司
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, AgencyDTO dto);

    /**
     * 删除经纪公司
     *
     * @param id 主键
     */
    void delete(Long id);
}
