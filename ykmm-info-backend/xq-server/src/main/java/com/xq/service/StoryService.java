package com.xq.service;

import com.xq.dto.StoryAuditDTO;
import com.xq.dto.StoryDTO;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.StoryVO;

import java.util.List;

/**
 * 剧情服务
 */
public interface StoryService {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<StoryVO> pageQuery(StoryPageQueryDTO query);

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 剧情详情
     */
    StoryVO getById(Long id);

    /**
     * 新增剧情
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    Long create(StoryDTO dto);

    /**
     * 编辑剧情
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, StoryDTO dto);

    /**
     * 删除剧情
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 审核剧情
     *
     * @param id         主键
     * @param dto        审核参数
     */
    void audit(Long id, StoryAuditDTO dto);

    /**
     * 根据分类ID查询剧情列表
     *
     * @param categoryId 分类ID
     * @return 剧情列表
     */
    List<StoryVO> listByCategoryId(Long categoryId);
}
