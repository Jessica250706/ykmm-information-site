package com.xq.mapper;

import com.xq.entity.RcOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RC 选项 Mapper
 */
@Mapper
public interface RcOptionMapper {
    /**
     * 根据 RC ID 查询选项
     */
    List<RcOption> listByRcId(@Param("rcId") Long rcId);

    /**
     * 根据版本ID查询选项
     */
    List<RcOption> listByVersionId(@Param("versionId") Long versionId);

    /**
     * 新增
     */
    int insert(RcOption option);

    /**
     * 批量新增
     */
    int insertBatch(@Param("list") List<RcOption> list);

    /**
     * 更新
     */
    int update(RcOption option);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除 RC 下的全部选项
     */
    int deleteByRcId(@Param("rcId") Long rcId);

    /**
     * 删除版本下的全部选项
     */
    int deleteByVersionId(@Param("versionId") Long versionId);

    /**
     * 根据版本ID批量查询RC选项
     *
     * @param versionIds 版本ID列表
     * @return RC选项列表
     */
    List<RcOption> listByVersionIds(@Param("versionIds") List<Long> versionIds);

    /**
     * 根据句子ID列表批量删除选项
     *
     * @param lineIds 句子ID列表
     * @return 影响行数
     */
    int deleteByLineIds(@Param("lineIds") List<Long> lineIds);

    /**
     * 删除所有引用某一行的 RC 选项
     *
     * @param lineId 句子ID
     * @return 影响行数
     */
    int deleteByLineId(@Param("lineId") Long lineId);
}
