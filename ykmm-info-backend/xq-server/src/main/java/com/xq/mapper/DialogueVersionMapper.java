package com.xq.mapper;

import com.xq.entity.DialogueVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DialogueVersionMapper {

    /**
     * 按来源查询对话版本
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @return 版本列表
     */
    List<DialogueVersion> listBySource(@Param("sourceType") Integer sourceType,
                                       @Param("sourceId") Long sourceId);
}
