package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.DialogueVersionContributorDTO;
import com.xq.dto.DialogueVersionContributorPageQueryDTO;
import com.xq.entity.DialogueVersionContributor;
import com.xq.enums.ContributorRoleEnum;
import com.xq.mapper.DialogueVersionContributorMapper;
import com.xq.result.PageResult;
import com.xq.service.DialogueVersionContributorService;
import com.xq.vo.DialogueVersionContributorVO;
import com.xq.vo.StoryContributorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话版本贡献者服务实现
 */
@Service
@Slf4j
public class DialogueVersionContributorServiceImpl implements DialogueVersionContributorService {

    @Autowired
    private DialogueVersionContributorMapper dialogueVersionContributorMapper;

    /**
     * 分页查询贡献者
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<DialogueVersionContributorVO> pageQuery(DialogueVersionContributorPageQueryDTO query) {
        if (query == null) {
            throw new RuntimeException("查询条件不能为空");
        }
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<DialogueVersionContributorVO> list = dialogueVersionContributorMapper.pageQuery(query);
        PageInfo<DialogueVersionContributorVO> pageInfo = new PageInfo<>(list);
        // 填充角色标签
        for (DialogueVersionContributorVO vo : pageInfo.getList()) {
            vo.setContributorRoleLabel(ContributorRoleEnum.getLabel(vo.getContributorRole()));
        }
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 根据版本ID查询贡献者列表
     *
     * @param versionId 版本ID
     * @return 贡献者列表
     */
    @Override
    public List<DialogueVersionContributorVO> listByVersionId(Long versionId) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        List<DialogueVersionContributorVO> list = dialogueVersionContributorMapper.listByVersionId(versionId);
        for (DialogueVersionContributorVO vo : list) {
            vo.setContributorRoleLabel(ContributorRoleEnum.getLabel(vo.getContributorRole()));
        }
        return list;
    }

    /**
     * 根据来源聚合查询贡献者
     *
     * @param sourceType 来源类型：1剧情 2RTV 3RC
     * @param sourceId   来源主键
     * @return 贡献者列表
     */
    @Override
    public List<StoryContributorVO> listContributorsBySource(Integer sourceType, Long sourceId) {
        if (sourceType == null) {
            throw new RuntimeException("来源类型不能为空");
        }
        if (sourceId == null) {
            throw new RuntimeException("来源ID不能为空");
        }
        return dialogueVersionContributorMapper.listContributorsBySource(sourceType, sourceId);
    }

    /**
     * 新增贡献者
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @Override
    @Transactional
    public Long create(DialogueVersionContributorDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getVersionId() == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        // 用户ID 和 姓名至少有一个
        boolean hasUser = dto.getUserId() != null;
        boolean hasName = dto.getContributorName() != null && !dto.getContributorName().isBlank();
        if (!hasUser && !hasName) {
            throw new RuntimeException("贡献者用户ID和姓名不能同时为空");
        }
        if (!ContributorRoleEnum.isValid(dto.getContributorRole())) {
            throw new RuntimeException("贡献者角色不合法");
        }

        // 幂等判断
        if (hasUser) {
            int exists = dialogueVersionContributorMapper.countByVersionAndUser(
                    dto.getVersionId(), dto.getUserId());
            if (exists > 0) {
                throw new RuntimeException("该用户已是当前版本的贡献者");
            }
        } else {
            int exists = dialogueVersionContributorMapper.countByVersionAndName(
                    dto.getVersionId(), dto.getContributorName());
            if (exists > 0) {
                throw new RuntimeException("该姓名已是当前版本的贡献者");
            }
        }

        DialogueVersionContributor entity = new DialogueVersionContributor();
        entity.setVersionId(dto.getVersionId());
        entity.setUserId(dto.getUserId());
        entity.setContributorName(hasName ? dto.getContributorName() : null);
        entity.setContributorRole(dto.getContributorRole());
        dialogueVersionContributorMapper.insert(entity);
        log.info("新增贡献者成功，versionId={}, userId={}, name={}",
                dto.getVersionId(), dto.getUserId(), dto.getContributorName());
        return entity.getId();
    }

    /**
     * 更新贡献者
     *
     * @param id  主键
     * @param dto 更新参数
     */
    @Override
    @Transactional
    public void update(Long id, DialogueVersionContributorDTO dto) {
        if (id == null) {
            throw new RuntimeException("贡献者ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        DialogueVersionContributor exist = dialogueVersionContributorMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("贡献者记录不存在");
        }
        if (dto.getContributorRole() != null && !ContributorRoleEnum.isValid(dto.getContributorRole())) {
            throw new RuntimeException("贡献者角色不合法");
        }

        DialogueVersionContributor entity = new DialogueVersionContributor();
        entity.setId(id);
        entity.setVersionId(dto.getVersionId());
        entity.setUserId(dto.getUserId());
        entity.setContributorRole(dto.getContributorRole());
        dialogueVersionContributorMapper.update(entity);
        log.info("更新贡献者成功，id={}", id);
    }

    /**
     * 删除贡献者
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("贡献者ID不能为空");
        }
        DialogueVersionContributor exist = dialogueVersionContributorMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("贡献者记录不存在");
        }
        dialogueVersionContributorMapper.deleteById(id);
        log.info("删除贡献者成功，id={}", id);
    }

    /**
     * 根据版本ID删除全部贡献者
     *
     * @param versionId 版本ID
     */
    @Override
    @Transactional
    public void deleteByVersionId(Long versionId) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        dialogueVersionContributorMapper.deleteByVersionId(versionId);
        log.info("清空版本贡献者成功，versionId={}", versionId);
    }

    /**
     * 判断用户是否是某个版本的贡献者
     *
     * @param versionId 版本ID
     * @param userId    用户ID
     * @return true 是，false 否
     */
    @Override
    public boolean isContributor(Long versionId, Long userId) {
        if (versionId == null || userId == null) {
            return false;
        }
        return dialogueVersionContributorMapper.countByVersionAndUser(versionId, userId) > 0;
    }

    /**
     * 判断用户是否是某个版本下指定角色的贡献者
     *
     * @param versionId 版本ID
     * @param userId    用户ID
     * @param role      角色值
     * @return true 是，false 否
     */
    @Override
    public boolean hasRole(Long versionId, Long userId, Integer role) {
        if (versionId == null || userId == null || role == null) {
            return false;
        }
        return dialogueVersionContributorMapper.countByVersionUserRole(versionId, userId, role) > 0;
    }

    /**
     * 记录版本创建时的贡献者信息
     *
     * @param versionId       版本ID
     * @param uploaderId      实际操作人ID
     * @param uploaderRole    实际操作人角色：1管理员 2普通用户
     * @param contributorId   内容归属用户ID，可为空
     * @param contributorName 内容归属用户姓名（contributorId 为空时使用）
     */
    @Override
    @Transactional
    public void recordCreator(Long versionId, Long uploaderId, Integer uploaderRole,
                              Long contributorId, String contributorName) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        if (uploaderId == null) {
            throw new RuntimeException("操作人ID不能为空");
        }

        boolean hasContributorId = contributorId != null;
        boolean hasContributorName = contributorName != null && !contributorName.isBlank();
        // 归属既没ID也没姓名时，默认归属为操作人自己
        Long finalContributorId = hasContributorId ? contributorId
                : (hasContributorName ? null : uploaderId);
        String finalContributorName = hasContributorName ? contributorName
                : (hasContributorId ? null : null);

        // 1. 内容作者
        if (!alreadyExists(versionId, finalContributorId, finalContributorName)) {
            DialogueVersionContributor author = new DialogueVersionContributor();
            author.setVersionId(versionId);
            author.setUserId(finalContributorId);
            author.setContributorName(finalContributorName);
            author.setContributorRole(ContributorRoleEnum.AUTHOR.getValue());
            dialogueVersionContributorMapper.insert(author);
        }

        // 2. 管理员代传时，额外记录代传管理员
        boolean isAdminDelegate = UserRoleConstant.ADMIN == (uploaderRole == null ? 0 : uploaderRole)
                && (!uploaderId.equals(finalContributorId) || hasContributorName);
        if (isAdminDelegate && !alreadyExists(versionId, uploaderId, null)) {
            DialogueVersionContributor delegate = new DialogueVersionContributor();
            delegate.setVersionId(versionId);
            delegate.setUserId(uploaderId);
            delegate.setContributorRole(ContributorRoleEnum.DELEGATE.getValue());
            dialogueVersionContributorMapper.insert(delegate);
        }

        log.info("记录贡献者成功，versionId={}, uploaderId={}, contributorId={}, contributorName={}",
                versionId, uploaderId, finalContributorId, finalContributorName);
    }

    @Override
    public void recordCreator(Long versionId, Long uploaderId, Integer uploaderRole, Long contributorId) {
        recordCreator(versionId, uploaderId, uploaderRole, contributorId, null);
    }

    /**
     * 判断贡献者是否已存在
     */
    private boolean alreadyExists(Long versionId, Long userId, String name) {
        if (userId != null) {
            return dialogueVersionContributorMapper.countByVersionAndUser(versionId, userId) > 0;
        }
        if (name != null && !name.isBlank()) {
            return dialogueVersionContributorMapper.countByVersionAndName(versionId, name) > 0;
        }
        return false;
    }

    /**
     * 查询贡献者详情
     *
     * @param id 主键
     * @return 贡献者详情
     */
    @Override
    public DialogueVersionContributorVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("贡献者ID不能为空");
        }
        DialogueVersionContributorVO vo = dialogueVersionContributorMapper.getVOById(id);
        if (vo == null) {
            throw new RuntimeException("贡献者不存在");
        }
        // 角色标签兜底（SQL 不算这个，Java 侧补）
        vo.setContributorRoleLabel(ContributorRoleEnum.getLabel(vo.getContributorRole()));
        // displayName 兜底（正常 SQL 已经算了）
        if (vo.getDisplayName() == null || vo.getDisplayName().isBlank()) {
            vo.setDisplayName(
                    vo.getNickname() != null ? vo.getNickname()
                            : vo.getContributorName() != null ? vo.getContributorName()
                            : "匿名"
            );
        }
        return vo;
    }
}
