package com.xq.service;

import com.xq.dto.CardCategoryDTO;
import com.xq.dto.CardCategoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.CardCategoryVO;

import java.util.List;

/**
 * 卡面类别服务
 */
public interface CardCategoryService {

    /**
     * 分页查询类别
     */
    PageResult<CardCategoryVO> pageQuery(CardCategoryPageQueryDTO query);

    /**
     * 查询全部类别（供下拉使用）
     */
    List<CardCategoryVO> listAll();

    /**
     * 查询类别详情
     */
    CardCategoryVO getById(Integer id);

    /**
     * 新增类别（仅管理员）
     *
     * @return 新增后的主键
     */
    Integer create(CardCategoryDTO dto);

    /**
     * 编辑类别（仅管理员）
     */
    void update(Integer id, CardCategoryDTO dto);

    /**
     * 删除类别（仅管理员，需保证未被引用）
     */
    void delete(Integer id);
}
