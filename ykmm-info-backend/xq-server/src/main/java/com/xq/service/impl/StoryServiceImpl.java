package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.StoryConstant;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.StoryAuditDTO;
import com.xq.dto.StoryDTO;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.entity.*;
import com.xq.enums.*;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.mapper.*;
import com.xq.result.PageResult;
import com.xq.service.StoryService;
import com.xq.support.ViewerFilterSupport;
import com.xq.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 剧情服务实现（三身份统一入口）
 * <p>
 * - 游客：仅已发布，只读
 * - 用户：已发布 + 自己创建的，可增改自己的
 * - 管理员：全部可见，全部操作
 */
@Service
@Slf4j
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryMapper storyMapper;
    @Autowired
    private StoryCategoryMapper storyCategoryMapper;
    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;
    @Autowired
    private DialogueLineMapper dialogueLineMapper;
    @Autowired
    private DialogueSegmentMapper dialogueSegmentMapper;
    @Autowired
    private DialogueImageMapper dialogueImageMapper;
    @Autowired
    private DialogueVersionContributorMapper dialogueVersionContributorMapper;
    @Autowired
    private RcOptionMapper rcOptionMapper;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private StickerMapper stickerMapper;
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 查询剧情分类树（无身份限制）
     *
     * @param categoryType 分类类型，null 返回全部
     * @return 分类树
     */
    @Override
    public List<StoryCategoryVO> categoryTree(Integer categoryType) {
        List<StoryCategory> all = (categoryType == null)
                ? storyCategoryMapper.listAll()
                : storyCategoryMapper.listByType(categoryType);

        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, StoryCategoryVO> idMap = new HashMap<>(all.size());
        for (StoryCategory c : all) {
            StoryCategoryVO vo = toCategoryVO(c);
            vo.setChildren(new ArrayList<>());
            idMap.put(vo.getId(), vo);
        }

        List<StoryCategoryVO> roots = new ArrayList<>();
        for (StoryCategoryVO vo : idMap.values()) {
            Long pid = vo.getParentId();
            if (pid == null || pid.equals(StoryConstant.ROOT_PARENT_ID) || !idMap.containsKey(pid)) {
                roots.add(vo);
            } else {
                idMap.get(pid).getChildren().add(vo);
            }
        }
        sortTree(roots);
        return roots;
    }

    /**
     * 分页查询剧情
     * <p>
     * - 管理员：不加过滤
     * - 游客：只查已发布
     * - 用户：已发布 + 自己创建的
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<StoryVO> pageQuery(StoryPageQueryDTO query) {
        if (query == null) {
            throw new RuntimeException("查询条件不能为空");
        }

        // 由角色上下文决定 status / viewerId
        Long viewerId = ViewerFilterSupport.apply(query::setStatus);

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Story> list = storyMapper.pageQuery(query, viewerId);
        PageInfo<Story> page = new PageInfo<>(list);

        if (page.getList().isEmpty()) {
            return new PageResult<>(page.getTotal(), Collections.emptyList());
        }

        List<StoryVO> voList = page.getList().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 查询剧情详情 - 简单版（不含对话版本）
     *
     * @param id 主键
     * @return 剧情 VO
     */
    @Override
    public StoryVO getById(Long id) {
        Story story = getStory(id);

        // 三身份可见性校验
        ViewerFilterSupport.checkCanView(story.getStatus(), story.getCreatorId());
        return toVO(story);
    }

    /**
     * 查询剧情详情（含对话版本）
     * <p>
     * - 先校验剧情本身对当前身份是否可见
     * - 再按身份过滤对话版本：
     *   - 管理员：全部版本
     *   - 游客：仅已发布版本
     *   - 用户：已发布 + 自己创建的版本
     *
     * @param id 剧情主键
     * @return 详情
     */
    @Override
    public StoryDetailVO storyDetail(Long id) {
        Story story = getStory(id);

        // 1. 剧情本身的可见性
        ViewerFilterSupport.checkCanView(story.getStatus(), story.getCreatorId());

        // 2. 基础字段 + 分类信息
        StoryDetailVO vo = new StoryDetailVO();
        BeanUtils.copyProperties(story, vo);
        StoryCategory category = storyCategoryMapper.getById(story.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
            vo.setCategoryType(category.getCategoryType());
            vo.setCategoryTypeLabel(StoryCategoryTypeEnum.getLabel(category.getCategoryType()));
        }

        // 3. 按身份计算版本过滤条件
        Integer statusFilter = null;
        Long viewerId = null;
        if (RoleContext.isGuest()) {
            statusFilter = StatusEnum.PUBLISHED.getValue();
        } else if (RoleContext.isUser()) {
            viewerId = BaseContext.getCurrentId();
        }

        // 4. 查版本（带身份过滤）
        List<DialogueVersion> versions = dialogueVersionMapper.listBySourceIds(
                DialogueSourceTypeEnum.STORY.getValue(),
                List.of(id),
                statusFilter,
                viewerId);

        if (versions == null || versions.isEmpty()) {
            vo.setVersions(Collections.emptyList());
            return vo;
        }

        // 5. 组装版本 VO
        vo.setVersions(buildVersionVOs(versions));
        return vo;
    }

    /**
     * 新增剧情
     * <p>
     * - 管理员 → 直接发布
     * - 用户 → 待审核
     * - 游客 → 拒绝
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @Override
    @Transactional
    public Long create(StoryDTO dto) {
        StoryCategory category = storyCategoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new RuntimeException("所属分类不存在");
        }

        Story story = new Story();
        BeanUtils.copyProperties(dto, story);
        if (story.getSort() == null) {
            story.setSort(0);
        }

        // 身份决定初始状态
        switch (UserRoleEnum.current()) {
            case ADMIN -> story.setStatus(StatusEnum.PUBLISHED.getValue());
            case USER  -> story.setStatus(StatusEnum.PENDING.getValue());
            case GUEST -> throw new UnauthorizedException("请先登录");
        }
        story.setCreatorId(BaseContext.getCurrentId());

        storyMapper.insert(story);
        log.info("新增剧情成功，id={}, status={}, creatorId={}", story.getId(), story.getStatus(), story.getCreatorId());
        return story.getId();
    }

    /**
     * 编辑剧情
     * <p>
     * - 管理员：可改任意剧情，状态保持原样
     * - 用户：只能改自己创建的，编辑后重置为待审核
     * - 游客：拒绝
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, StoryDTO dto) {
        Story exist = storyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情不存在");
        }

        if (dto.getCategoryId() != null) {
            StoryCategory category = storyCategoryMapper.getById(dto.getCategoryId());
            if (category == null) {
                throw new RuntimeException("所属分类不存在");
            }
        }

        Story story = new Story();
        BeanUtils.copyProperties(dto, story);
        story.setId(id);

        switch (UserRoleEnum.current()) {
            case ADMIN -> { /* 不改状态 */ }
            case USER -> {
                if (!Objects.equals(exist.getCreatorId(), BaseContext.getCurrentId())) {
                    throw new ForbiddenException("只能修改自己创建的剧情");
                }
                // 用户编辑后重新进入待审核
                story.setStatus(StatusEnum.PENDING.getValue());
            }
            case GUEST -> throw new UnauthorizedException("请先登录");
        }

        storyMapper.update(story);
        log.info("编辑剧情成功，id={}, role={}, status={}", id, UserRoleEnum.current(), story.getStatus());
    }

    /**
     * 删除剧情（仅管理员）
     * <p>
     * 剧情下可能含他人贡献的对话版本，为避免"创建者删除连带删除他人贡献"，
     * 限制为仅管理员可删除。
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 权限：仅管理员
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能删除剧情，请联系管理员");
        }

        Story exist = storyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情不存在");
        }

        // 级联删除对话版本及其内容
        List<Long> versionIds = dialogueVersionMapper.listIdsBySource(
                DialogueSourceTypeEnum.STORY.getValue(), List.of(id));

        if (!versionIds.isEmpty()) {
            List<Long> lineIds = dialogueLineMapper.listIdsByVersionIds(versionIds);
            if (!lineIds.isEmpty()) {
                dialogueSegmentMapper.deleteByLineIds(lineIds);
                dialogueLineMapper.deleteByIds(lineIds);
                rcOptionMapper.deleteByVersionIds(versionIds);
            }
            dialogueImageMapper.deleteByVersionIds(versionIds);
            dialogueVersionContributorMapper.deleteByVersionIds(versionIds);
            dialogueVersionMapper.deleteByIds(versionIds);
        }

        storyMapper.deleteById(id);
        log.info("删除剧情成功，id={}, 操作人={}, 角色={}", id, BaseContext.getCurrentId(), UserRoleEnum.current());
    }

    /**
     * 审核剧情（仅管理员）
     *
     * @param id  主键
     * @param dto 审核参数
     */
    @Override
    @Transactional
    public void audit(Long id, StoryAuditDTO dto) {
        // 权限：仅管理员
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能审核剧情");
        }

        Story exist = storyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情不存在");
        }
        if (!StatusEnum.PENDING.getValue().equals(exist.getStatus())) {
            throw new RuntimeException("该剧情不在待审核状态");
        }

        Story story = new Story();
        story.setId(id);
        story.setStatus(dto.getStatus());
        story.setReviewerId(BaseContext.getCurrentId());
        story.setReviewTime(LocalDateTime.now());
        story.setReviewRemark(dto.getReviewRemark());
        storyMapper.updateStatus(story);
        log.info("审核剧情成功，id={}, status={}", id, dto.getStatus());
    }

    /**
     * 根据分类ID查询剧情列表（管理端用，不过滤身份）
     *
     * @param categoryId 分类ID
     * @return 剧情列表
     */
    @Override
    public List<StoryVO> listByCategoryId(Long categoryId) {
        List<Story> stories = storyMapper.listByCategoryId(categoryId);
        if (stories == null || stories.isEmpty()) {
            return Collections.emptyList();
        }

        StoryCategory category = storyCategoryMapper.getById(categoryId);
        List<StoryVO> voList = new ArrayList<>(stories.size());
        for (Story story : stories) {
            StoryVO vo = new StoryVO();
            BeanUtils.copyProperties(story, vo);
            vo.setStatusLabel(StatusEnum.getLabel(story.getStatus()));
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryType(category.getCategoryType());
                vo.setCategoryTypeLabel(
                        StoryCategoryTypeEnum.getLabel(category.getCategoryType()));
            }
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 组装版本 VO 列表（含句子、片段、表情包、说话人、图片）
     *
     * @param versions 版本实体列表
     * @return 版本 VO 列表
     */
    private List<DialogueVersionVO> buildVersionVOs(List<DialogueVersion> versions) {
        List<Long> versionIds = versions.stream()
                .map(DialogueVersion::getId)
                .toList();

        // 1. 批量查所有行、图片，按 versionId 分组
        List<DialogueLine> allLines = dialogueLineMapper.listByVersionIds(versionIds);
        List<DialogueImage> allImages = dialogueImageMapper.listByVersionIds(versionIds);

        Map<Long, List<DialogueLine>> linesByVersion = allLines.stream()
                .collect(Collectors.groupingBy(DialogueLine::getVersionId));
        Map<Long, List<DialogueImage>> imagesByVersion = allImages.stream()
                .collect(Collectors.groupingBy(DialogueImage::getVersionId));

        // 2. 片段：基于所有 lineIds 批量查
        List<Long> allLineIds = allLines.stream().map(DialogueLine::getId).toList();
        Map<Long, List<DialogueSegment>> segmentsByLine = allLineIds.isEmpty()
                ? Collections.emptyMap()
                : dialogueSegmentMapper.listByLineIds(allLineIds).stream()
                .collect(Collectors.groupingBy(DialogueSegment::getLineId));

        // 3. 说话人：speakerId -> role -> person
        Set<Long> speakerIds = allLines.stream()
                .map(DialogueLine::getSpeakerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Role> roleMap = speakerIds.isEmpty()
                ? Collections.emptyMap()
                : roleMapper.listByIds(speakerIds).stream()
                .collect(Collectors.toMap(Role::getId, r -> r, (a, b) -> a));

        Set<Long> personIds = roleMap.values().stream()
                .map(Role::getPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Person> personMap = personIds.isEmpty()
                ? Collections.emptyMap()
                : personMapper.listByIds(personIds).stream()
                .collect(Collectors.toMap(Person::getId, p -> p, (a, b) -> a));

        // 4. 表情包：基于 stickerId 批量查
        Set<Long> stickerIds = segmentsByLine.values().stream()
                .flatMap(List::stream)
                .map(DialogueSegment::getStickerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Sticker> stickerMap = stickerIds.isEmpty()
                ? Collections.emptyMap()
                : stickerMapper.listByIds(stickerIds).stream()
                .collect(Collectors.toMap(Sticker::getId, s -> s, (a, b) -> a));

        // 5. 组装每个版本
        List<DialogueVersionVO> versionVOs = new ArrayList<>(versions.size());
        for (DialogueVersion version : versions) {
            DialogueVersionVO vvo = new DialogueVersionVO();
            BeanUtils.copyProperties(version, vvo);
            vvo.setLanguageLabel(DialogueLanguageEnum.getLabel(version.getLanguage()));
            vvo.setFormatLabel(DialogueFormatEnum.getLabel(version.getFormat()));
            vvo.setScopeLabel(DialogueScopeEnum.getLabel(version.getScope()));

            // 5.1 行 + 片段 + 说话人
            List<DialogueLine> lines =
                    linesByVersion.getOrDefault(version.getId(), Collections.emptyList());
            List<DialogueLineVO> lineVOs = new ArrayList<>(lines.size());
            for (DialogueLine line : lines) {
                DialogueLineVO lvo = new DialogueLineVO();
                BeanUtils.copyProperties(line, lvo);

                Role role = roleMap.get(line.getSpeakerId());
                if (role != null) {
                    lvo.setSpeakerName(role.getName());
                    lvo.setPersonId(role.getPersonId());

                    Person person = personMap.get(role.getPersonId());
                    if (person != null) {
                        lvo.setPersonNameCn(person.getNameCn());
                        lvo.setPersonAvatar(person.getAvatar());
                        lvo.setPersonThemeColor(person.getThemeColor());
                    }
                }

                List<DialogueSegment> segs =
                        segmentsByLine.getOrDefault(line.getId(), Collections.emptyList());
                List<DialogueSegmentVO> segVOs = new ArrayList<>(segs.size());
                for (DialogueSegment seg : segs) {
                    DialogueSegmentVO svo = new DialogueSegmentVO();
                    BeanUtils.copyProperties(seg, svo);
                    if (seg.getStickerId() != null) {
                        Sticker sticker = stickerMap.get(seg.getStickerId());
                        if (sticker != null) {
                            svo.setStickerImageUrl(sticker.getImageUrl());
                            svo.setStickerEmoji(sticker.getEmoji());
                            svo.setStickerLabel(sticker.getLabel());
                        }
                    }
                    segVOs.add(svo);
                }
                lvo.setSegments(segVOs);
                lineVOs.add(lvo);
            }
            vvo.setLines(lineVOs);

            // 5.2 图片
            List<DialogueImage> images =
                    imagesByVersion.getOrDefault(version.getId(), Collections.emptyList());
            List<DialogueImageVO> imageVOs = new ArrayList<>(images.size());
            for (DialogueImage img : images) {
                DialogueImageVO ivo = new DialogueImageVO();
                BeanUtils.copyProperties(img, ivo);
                imageVOs.add(ivo);
            }
            vvo.setImages(imageVOs);

            versionVOs.add(vvo);
        }
        return versionVOs;
    }

    /**
     * StoryCategory -> StoryCategoryVO
     */
    private StoryCategoryVO toCategoryVO(StoryCategory c) {
        if (c == null) return null;
        StoryCategoryVO vo = new StoryCategoryVO();
        BeanUtils.copyProperties(c, vo);
        vo.setCategoryTypeLabel(StoryCategoryTypeEnum.getLabel(c.getCategoryType()));
        return vo;
    }

    /**
     * Story -> StoryVO
     */
    private StoryVO toVO(Story story) {
        if (story == null) return null;
        StoryVO vo = new StoryVO();
        BeanUtils.copyProperties(story, vo);
        vo.setStatusLabel(StatusEnum.getLabel(story.getStatus()));

        if (story.getCategoryId() != null) {
            StoryCategory category = storyCategoryMapper.getById(story.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryType(category.getCategoryType());
                vo.setCategoryTypeLabel(
                        StoryCategoryTypeEnum.getLabel(category.getCategoryType()));
            }
        }
        return vo;
    }

    /**
     * 分类树按 sort/id 排序（递归）
     */
    private void sortTree(List<StoryCategoryVO> list) {
        list.sort(Comparator
                .comparing(StoryCategoryVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(StoryCategoryVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (StoryCategoryVO vo : list) {
            if (vo.getChildren() != null && !vo.getChildren().isEmpty()) {
                sortTree(vo.getChildren());
            }
        }
    }

    private @NotNull Story getStory(Long id) {
        if (id == null) {
            throw new RuntimeException("剧情ID不能为空");
        }
        Story story = storyMapper.getById(id);
        if (story == null) {
            throw new RuntimeException("剧情不存在");
        }
        return story;
    }
}
