package com.xq.mapper;

import com.github.pagehelper.Page;
import com.xq.dto.StickerGroupPageQueryDTO;
import com.xq.entity.StickerGroup;
import com.xq.vo.StickerGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 表情包分组Mapper
 */
@Mapper
public interface StickerGroupMapper {

    /**
     * 分页查询表情包分组
     */
    Page<StickerGroupVO> pageQuery(StickerGroupPageQueryDTO queryDTO);

    /**
     * 查询所有表情包分组
     */
    List<StickerGroupVO> listAll();

    /**
     * 根据ID查询表情包分组VO
     */
    StickerGroupVO getVOById(@Param("id") Long id);

    /**
     * 根据ID查询表情包分组实体
     */
    StickerGroup getEntityById(@Param("id") Long id);

    /**
     * 根据名称统计数量，excludeId用于更新时排除自身
     */
    long countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 新增表情包分组
     */
    int insert(StickerGroup stickerGroup);

    /**
     * 更新表情包分组
     */
    int update(StickerGroup stickerGroup);

    /**
     * 根据ID删除表情包分组
     */
    int deleteById(@Param("id") Long id);
}
