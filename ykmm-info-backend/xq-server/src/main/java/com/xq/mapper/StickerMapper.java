package com.xq.mapper;

import com.github.pagehelper.Page;
import com.xq.dto.StickerPageQueryDTO;
import com.xq.entity.Sticker;
import com.xq.vo.StickerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 表情包Mapper
 */
@Mapper
public interface StickerMapper {

    /**
     * 条件查询表情包列表
     */
    List<Sticker> listByCondition(@Param("keyword") String keyword,
                                  @Param("stickerType") Integer stickerType);

    /**
     * 根据 id 查询表情包
     */
    Sticker getById(@Param("id") Long id);

    /**
     * 按 ID 集合批量查询表情包
     *
     * @param ids 主键集合
     * @return 表情包列表
     */
    List<Sticker> listByIds(@Param("ids") Collection<Long> ids);

    /**
     * 根据标签查询表情包
     *
     * @param label 标签
     * @return 表情包
     */
    Sticker getByLabel(@Param("label") String label);

    /**
     * 分页查询表情包
     */
    Page<StickerVO> pageQuery(StickerPageQueryDTO queryDTO);

    /**
     * 根据ID查询表情包VO
     */
    StickerVO getVOById(@Param("id") Long id);

    /**
     * 根据ID查询表情包实体
     */
    Sticker getEntityById(@Param("id") Long id);

    /**
     * 新增表情包
     */
    int insert(Sticker sticker);

    /**
     * 更新表情包
     */
    int update(Sticker sticker);

    /**
     * 根据ID删除表情包
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计分组下表情包数量
     */
    long countByGroupId(@Param("groupId") Long groupId);
}
