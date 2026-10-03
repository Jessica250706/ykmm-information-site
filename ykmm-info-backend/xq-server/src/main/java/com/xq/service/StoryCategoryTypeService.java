package com.xq.service;

import com.xq.dto.StoryCategoryTypeDTO;
import com.xq.vo.StoryCategoryTypeVO;

import java.util.List;

/**
 * 剧情分类类型服务
 */
public interface StoryCategoryTypeService {

    /**
     * 查询全部
     *
     * @return 列表
     */
    List<StoryCategoryTypeVO> listAll();

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 详情
     */
    StoryCategoryTypeVO getById(Integer id);

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Integer id, StoryCategoryTypeDTO dto);
}
