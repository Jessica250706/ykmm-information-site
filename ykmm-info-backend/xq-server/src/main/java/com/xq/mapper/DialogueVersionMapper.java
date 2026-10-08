package com.xq.mapper;

import com.xq.dto.DialogueVersionPageQueryDTO;
import com.xq.entity.DialogueVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话版本 Mapper
 */
@Mapper
public interface DialogueVersionMapper {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 版本列表
     */
    List<DialogueVersion> pageQuery(@Param("query") DialogueVersionPageQueryDTO query);

    /**
     * 查询某来源下全部版本
     *
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     * @return 版本列表
     */
    List<DialogueVersion> listBySource(@Param("sourceType") Integer sourceType,
                                       @Param("sourceId") Long sourceId);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 版本
     */
    DialogueVersion getById(@Param("id") Long id);

    /**
     * 查询同来源下是否存在同组合版本
     *
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     * @param language   语言
     * @param format     形式
     * @param scope      范围
     * @return 已存在的版本
     */
    DialogueVersion getByUniqueKey(@Param("sourceType") Integer sourceType,
                                   @Param("sourceId") Long sourceId,
                                   @Param("language") Integer language,
                                   @Param("format") Integer format,
                                   @Param("scope") Integer scope);

    /**
     * 新增
     *
     * @param version 版本
     * @return 影响行数
     */
    int insert(DialogueVersion version);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按来源 + 格式查询版本
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @param format     格式：1文字 2图片
     * @return 版本列表
     */
    List<DialogueVersion> listBySourceAndFormat(@Param("sourceType") Integer sourceType,
                                                @Param("sourceId") Long sourceId,
                                                @Param("format") Integer format);

    /**
     * 根据来源类型批量查询对话版本
     *
     * @param sourceType 来源类型
     * @param sourceIds  来源ID列表
     * @param status     审核状态过滤，null 表示不过滤
     * @return 对话版本列表
     */
    List<DialogueVersion> listBySourceIds(@Param("sourceType") Integer sourceType,
                                          @Param("sourceIds") List<Long> sourceIds,
                                          @Param("status") Integer status);
}
