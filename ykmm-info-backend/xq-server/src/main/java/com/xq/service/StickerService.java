package com.xq.service;

import com.github.pagehelper.PageInfo;
import com.xq.dto.StickerDTO;
import com.xq.dto.StickerPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.StickerVO;

/**
 * 表情包Service
 */
public interface StickerService {

    /**
     * 分页查询表情包
     */
    PageResult<StickerVO> pageQuery(StickerPageQueryDTO queryDTO);

    /**
     * 根据ID查询表情包
     */
    StickerVO getById(Long id);

    /**
     * 新增表情包
     */
    void save(StickerDTO stickerDTO);

    /**
     * 更新表情包
     */
    void update(StickerDTO stickerDTO);

    /**
     * 删除表情包
     */
    void delete(Long id);
}
