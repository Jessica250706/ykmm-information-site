package com.xq.mapper;

import com.xq.entity.DialogueLine;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DialogueLineMapper {

    /**
     * 按版本批量查询对话行
     *
     * @param versionIds 版本ID列表
     * @return 行列表
     */
    List<DialogueLine> listByVersionIds(@Param("versionIds") List<Long> versionIds);
}
