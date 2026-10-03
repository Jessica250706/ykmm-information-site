package com.xq.mapper;

import com.xq.entity.DialogueSegment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DialogueSegmentMapper {

    /**
     * 按行批量查询片段
     *
     * @param lineIds 行ID列表
     * @return 片段列表
     */
    List<DialogueSegment> listByLineIds(@Param("lineIds") List<Long> lineIds);
}
