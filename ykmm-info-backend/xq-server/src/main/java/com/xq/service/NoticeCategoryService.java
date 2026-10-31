package com.xq.service;

import com.xq.dto.NoticeCategoryDTO;
import com.xq.vo.NoticeCategoryVO;

import java.util.List;

/**
 * 公告分类服务
 */
public interface NoticeCategoryService {

    /**
     * 全部列表（游客可访问，按 sort 升序）
     */
    List<NoticeCategoryVO> listAll();

    void create(NoticeCategoryDTO dto);

    void update(Integer id, NoticeCategoryDTO dto);

    /**
     * 删除（分类下仍有公告时会拒绝）
     */
    void delete(Integer id);
}