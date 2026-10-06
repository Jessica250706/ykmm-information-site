package com.xq.service;

import com.xq.dto.DialogueVersionContributorDTO;
import com.xq.dto.DialogueVersionContributorPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.DialogueVersionContributorVO;
import com.xq.vo.StoryContributorVO;

import java.util.List;

/**
 * 对话版本贡献者服务
 */
public interface DialogueVersionContributorService {

    /**
     * 分页查询贡献者
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<DialogueVersionContributorVO> pageQuery(DialogueVersionContributorPageQueryDTO query);

    /**
     * 根据版本ID查询贡献者列表
     *
     * @param versionId 版本ID
     * @return 贡献者列表
     */
    List<DialogueVersionContributorVO> listByVersionId(Long versionId);

    /**
     * 根据来源聚合查询贡献者
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @return 贡献者列表
     */
    List<StoryContributorVO> listContributorsBySource(Integer sourceType, Long sourceId);

    /**
     * 新增贡献者
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    Long create(DialogueVersionContributorDTO dto);

    /**
     * 更新贡献者
     *
     * @param id  主键
     * @param dto 更新参数
     */
    void update(Long id, DialogueVersionContributorDTO dto);

    /**
     * 删除贡献者
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 根据版本ID删除全部贡献者
     *
     * @param versionId 版本ID
     */
    void deleteByVersionId(Long versionId);

    /**
     * 判断用户是否是某个版本的贡献者（任意角色）
     *
     * @param versionId 版本ID
     * @param userId    用户ID
     * @return true 是，false 否
     */
    boolean isContributor(Long versionId, Long userId);

    /**
     * 判断用户是否是某个版本下指定角色的贡献者
     *
     * @param versionId 版本ID
     * @param userId    用户ID
     * @param role      角色值
     * @return true 是，false 否
     */
    boolean hasRole(Long versionId, Long userId, Integer role);

    /**
     * 记录版本创建时的贡献者信息
     * - 用户本人上传：插入一条 (uploaderId, AUTHOR)
     * - 管理员代传：插入 (contributorId, AUTHOR) + (uploaderId, DELEGATE)
     *
     * @param versionId     版本ID
     * @param uploaderId    实际操作人ID
     * @param uploaderRole  实际操作人角色：1管理员 2普通用户
     * @param contributorId 内容归属用户ID，可为空；为空时默认等于 uploaderId
     */
    void recordCreator(Long versionId, Long uploaderId, Integer uploaderRole, Long contributorId);

    /**
     * 记录版本创建时的贡献者信息（无账号姓名版本）
     *
     * @param versionId       版本ID
     * @param uploaderId      实际操作人ID
     * @param uploaderRole    实际操作人角色：1管理员 2普通用户
     * @param contributorId   内容归属用户ID，可为空
     * @param contributorName 内容归属用户姓名（contributorId 为空时使用）
     */
    void recordCreator(Long versionId, Long uploaderId, Integer uploaderRole,
                       Long contributorId, String contributorName);

    /**
     * 查询贡献者详情
     *
     * @param id 主键
     * @return 贡献者详情
     */
    DialogueVersionContributorVO getById(Long id);
}
