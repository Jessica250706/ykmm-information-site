package com.xq.mapper;

import com.xq.dto.DialogueVersionContributorPageQueryDTO;
import com.xq.entity.DialogueVersionContributor;
import com.xq.vo.DialogueVersionContributorVO;
import com.xq.vo.StoryContributorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话版本贡献者 Mapper
 */
@Mapper
public interface DialogueVersionContributorMapper {

    /**
     * 分页查询贡献者
     *
     * @param query 分页查询条件
     * @return 贡献者列表
     */
    List<DialogueVersionContributorVO> pageQuery(@Param("query") DialogueVersionContributorPageQueryDTO query);

    /**
     * 根据版本ID查询贡献者列表
     *
     * @param versionId 版本ID
     * @return 贡献者列表
     */
    List<DialogueVersionContributorVO> listByVersionId(@Param("versionId") Long versionId);

    /**
     * 根据来源聚合查询贡献者（仅返回内容作者角色）
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @return 按用户聚合的贡献者列表
     */
    List<StoryContributorVO> listContributorsBySource(@Param("sourceType") Integer sourceType,
                                                      @Param("sourceId") Long sourceId);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 贡献者
     */
    DialogueVersionContributor getById(@Param("id") Long id);

    /**
     * 新增
     *
     * @param contributor 贡献者实体
     * @return 影响行数
     */
    int insert(DialogueVersionContributor contributor);

    /**
     * 更新
     *
     * @param contributor 贡献者实体
     * @return 影响行数
     */
    int update(DialogueVersionContributor contributor);

    /**
     * 根据 id 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据版本ID删除
     *
     * @param versionId 版本ID
     * @return 影响行数
     */
    int deleteByVersionId(@Param("versionId") Long versionId);

    /**
     * 批量删除
     *
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 统计版本下该用户是否已是贡献者
     *
     * @param versionId 版本ID
     * @param userId    用户ID
     * @return 数量
     */
    int countByVersionAndUser(@Param("versionId") Long versionId,
                              @Param("userId") Long userId);

    /**
     * 判断用户是否是某个版本下指定角色的贡献者
     *
     * @param versionId 版本ID
     * @param userId    用户ID
     * @param role      角色值
     * @return 数量
     */
    int countByVersionUserRole(@Param("versionId") Long versionId,
                               @Param("userId") Long userId,
                               @Param("role") Integer role);

    /**
     * 统计版本下某姓名是否已存在（用于无账号贡献者去重）
     *
     * @param versionId       版本ID
     * @param contributorName 姓名
     * @return 数量
     */
    int countByVersionAndName(@Param("versionId") Long versionId,
                              @Param("contributorName") String contributorName);
}
