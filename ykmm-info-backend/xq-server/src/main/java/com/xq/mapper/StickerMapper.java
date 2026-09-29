package com.xq.mapper;

import com.xq.entity.Sticker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}
