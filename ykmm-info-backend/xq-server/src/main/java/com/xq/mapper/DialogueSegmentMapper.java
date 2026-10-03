package com.xq.mapper;

import com.xq.entity.DialogueSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话片段 Mapper
 */
@Mapper
public interface DialogueSegmentMapper {

    /**
     * 按句子查询片段
     *
     * @param lineId 句子ID
     * @return 片段列表
     */
    List<DialogueSegment> listByLineId(@Param("lineId") Long lineId);

    /**
     * 按句子ID列表批量查询
     *
     * @param lineIds 句子ID列表
     * @return 片段列表
     */
    List<DialogueSegment> listByLineIds(@Param("lineIds") List<Long> lineIds);

    /**
     * 批量新增
     *
     * @param segments 片段列表
     * @return 影响行数
     */
    int insertBatch(@Param("segments") List<DialogueSegment> segments);

    /**
     * 删除句子的全部片段
     *
     * @param lineId 句子ID
     * @return 影响行数
     */
    int deleteByLineId(@Param("lineId") Long lineId);

    /**
     * 按句子ID列表批量删除
     *
     * @param lineIds 句子ID列表
     * @return 影响行数
     */
    int deleteByLineIds(@Param("lineIds") List<Long> lineIds);
}
