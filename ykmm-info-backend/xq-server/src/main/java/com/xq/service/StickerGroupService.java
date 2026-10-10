package com.xq.service;

import com.github.pagehelper.PageInfo;
import com.xq.dto.StickerGroupDTO;
import com.xq.dto.StickerGroupPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.StickerGroupVO;
import com.xq.vo.StickerGroupWithStickersVO;

import java.util.List;

/**
 * 表情包分组Service
 */
public interface StickerGroupService {

    /**
     * 分页查询表情包分组
     */
    PageResult<StickerGroupVO> pageQuery(StickerGroupPageQueryDTO queryDTO);

    /**
     * 查询所有表情包分组
     */
    List<StickerGroupVO> listAll();

    /**
     * 根据ID查询表情包分组
     */
    StickerGroupVO getById(Long id);

    /**
     * 新增表情包分组
     */
    void save(StickerGroupDTO stickerGroupDTO);

    /**
     * 更新表情包分组
     */
    void update(StickerGroupDTO stickerGroupDTO);

    /**
     * 删除表情包分组
     */
    void delete(Long id);

    /**
     * 查询所有分组及其表情包（用于表情选择器）
     */
    List<StickerGroupWithStickersVO> listWithStickers();
}
