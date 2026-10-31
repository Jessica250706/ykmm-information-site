package com.xq.mapper;

import com.xq.entity.SysNoticeCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告分类 Mapper
 */
@Mapper
public interface NoticeCategoryMapper {

    List<SysNoticeCategory> listAll();

    SysNoticeCategory getById(@Param("id") Integer id);

    int insert(SysNoticeCategory category);

    int update(SysNoticeCategory category);

    int deleteById(@Param("id") Integer id);
}
