package com.xq.service;

import com.xq.dto.StoryCategoryDTO;
import com.xq.dto.StoryCategoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.StoryCategoryVO;

import java.util.List;

/**
 * 剧情分类服务
 */
public interface StoryCategoryService {

    /**
     * 查询剧情分类树
     *
     * @param categoryType 分类类型，null 返回全部
     * @return 分类树
     */
    List<StoryCategoryVO> listTree(Integer categoryType);

    /**
     * 分页查询剧情分类树
     *
     * @param query 查询条件
     * @return 分类树
     */
    PageResult<StoryCategoryVO> pageTree(StoryCategoryPageQueryDTO query);

    /**
     * 查询分类详情
     *
     * @param id 主键
     * @return 分类详情
     */
    StoryCategoryVO getById(Long id);

    /**
     * 新增分类
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    Long create(StoryCategoryDTO dto);

    /**
     * 编辑分类
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, StoryCategoryDTO dto);

    /**
     * 删除分类
     *
     * @param id 主键
     */
    void delete(Long id);
}
