package com.xq.mapper;

import com.xq.entity.DialogueLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 对话句子 Mapper
 */
@Mapper
public interface DialogueLineMapper {

    /**
     * 按版本查询全部句子
     *
     * @param versionId 版本ID
     * @return 句子列表
     */
    List<DialogueLine> listByVersionId(@Param("versionId") Long versionId);

    /**
     * 查询版本下所有句子ID
     *
     * @param versionId 版本ID
     * @return 句子ID列表
     */
    List<Long> listIdsByVersionId(@Param("versionId") Long versionId);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 句子
     */
    DialogueLine getById(@Param("id") Long id);

    /**
     * 新增
     *
     * @param line 句子
     * @return 影响行数
     */
    int insert(DialogueLine line);

    /**
     * 批量新增
     *
     * @param lines 句子列表
     * @return 影响行数
     */
    int insertBatch(@Param("lines") List<DialogueLine> lines);

    /**
     * 更新
     *
     * @param line 句子
     * @return 影响行数
     */
    int update(DialogueLine line);

    /**
     * 更新排序
     *
     * @param id   句子ID
     * @param sort 排序
     * @return 影响行数
     */
    int updateSort(@Param("id") Long id, @Param("sort") Integer sort);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按版本删除全部句子
     *
     * @param versionId 版本ID
     * @return 影响行数
     */
    int deleteByVersionId(@Param("versionId") Long versionId);

    /**
     * 统计版本下句子数量
     *
     * @param versionId 版本ID
     * @return 数量
     */
    int countByVersionId(@Param("versionId") Long versionId);

    /**
     * 查询版本下最大排序值
     *
     * @param versionId 版本ID
     * @return 最大 sort
     */
    Integer getMaxSort(@Param("versionId") Long versionId);

    /**
     * 按版本ID批量查询行
     */
    List<DialogueLine> listByVersionIds(@Param("versionIds") Collection<Long> versionIds);

    /**
     * 根据ID列表批量删除
     *
     * @param ids 句子ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);
}
