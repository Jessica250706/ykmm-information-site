package com.xq.service;

import com.xq.dto.IdolGroupDTO;
import com.xq.vo.IdolGroupVO;

import java.util.List;

/**
 * 偶像团体服务
 */
public interface IdolGroupService {

    /**
     * 查询全部偶像团体
     *
     * @return 偶像团体列表
     */
    List<IdolGroupVO> listAll();

    /**
     * 根据 id 查询详情
     *
     * @param id 主键
     * @return 偶像团体详情
     */
    IdolGroupVO getById(Long id);

    /**
     * 新增偶像团体
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    Long create(IdolGroupDTO dto);

    /**
     * 编辑偶像团体
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, IdolGroupDTO dto);

    /**
     * 删除偶像团体
     *
     * @param id 主键
     */
    void delete(Long id);
}
