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
    List<DialogueVersion> pageQuery(@Param("query") DialogueVersionPageQueryDTO query, @Param("viewerId") Long viewerId);

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
     * 根据来源类型 + 来源ID列表，批量查询对话版本
     *
     * @param sourceType   来源类型
     * @param sourceIds    来源ID列表
     * @param statusFilter 状态过滤：null 不过滤；1 只查已发布
     * @param viewerId     访客ID：null 不按创建者过滤；非 null 则查"已发布 OR 自己创建的"
     * @return 版本列表
     */
    List<DialogueVersion> listBySourceIds(@Param("sourceType") Integer sourceType,
                                          @Param("sourceIds") List<Long> sourceIds,
                                          @Param("statusFilter") Integer statusFilter,
                                          @Param("viewerId") Long viewerId);

    /**
     * 根据来源类型 + 来源ID列表查询版本主键
     * <p>用于级联删除卡面时，先查出下属所有版本主键</p>
     *
     * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     * @param sourceIds  来源ID列表（不能为空）
     * @return 版本主键列表
     */
    List<Long> listIdsBySource(@Param("sourceType") Integer sourceType,
                               @Param("sourceIds") List<Long> sourceIds);

    /**
     * 根据主键列表批量删除
     *
     * @param ids 主键列表（不能为空）
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 分页查询指定用户创建的版本（含所有状态）
     */
    List<DialogueVersion> pageQueryByCreator(@Param("query") DialogueVersionPageQueryDTO query,
                                             @Param("creatorId") Long creatorId);

    /**
     * 审核对话版本：更新审核相关字段
     * <p>
     * 注意：这里用固定 SQL 更新 status / reviewerId / reviewTime / reviewRemark 四个字段，
     * 而不是复用通用动态 update —— 因为 reviewRemark 在"通过"时可能被显式写成空串以清除旧备注，
     * 动态 <if test="!= null"> 无法做到"清空"。
     *
     * @param version 待更新的审核字段（至少包含 id / status）
     */
    void review(DialogueVersion version);

    /**
     * 将对话版本置为待审核，并清空上一次审核记录
     * <p>
     * 幂等：已处于待审核状态时不再更新（SQL 里带 {@code AND status != 2}），
     * 避免用户连续编辑同一版本时反复写库。
     *
     * @param id 版本ID
     */
    void markPending(@Param("id") Long id);
}
