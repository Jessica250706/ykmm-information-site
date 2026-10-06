package com.xq.service;

import com.xq.dto.CardSeriesDTO;
import com.xq.dto.CardSeriesPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.CardSeriesVO;

import java.util.List;

/**
 * 卡面系列服务
 */
public interface CardSeriesService {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<CardSeriesVO> pageQuery(CardSeriesPageQueryDTO query);

    /**
     * 查询全部（下拉用）
     *
     * @return 系列列表
     */
    List<CardSeriesVO> listAll();

    /**
     * 按名称搜索（下拉用，支持远程搜索）
     *
     * @param keyword 关键字
     * @return 系列列表
     */
    List<CardSeriesVO> searchByName(String keyword);

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 系列详情
     */
    CardSeriesVO getById(Long id);

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    Long create(CardSeriesDTO dto);

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, CardSeriesDTO dto);

    /**
     * 删除
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 按名称查询或创建（前端下拉允许"输入不存在则新增"场景）
     *
     * @param name 系列名
     * @return 系列ID
     */
    Long findOrCreateByName(String name);
}
