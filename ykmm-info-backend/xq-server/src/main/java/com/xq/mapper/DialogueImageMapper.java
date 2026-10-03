package com.xq.mapper;

import com.xq.entity.DialogueImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DialogueImageMapper {

    /**
     * 按版本批量查询图片
     *
     * @param versionIds 版本ID列表
     * @return 图片列表
     */
    List<DialogueImage> listByVersionIds(@Param("versionIds") List<Long> versionIds);
}
