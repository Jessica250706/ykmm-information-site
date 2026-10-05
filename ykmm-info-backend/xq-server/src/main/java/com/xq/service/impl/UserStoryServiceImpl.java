package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.StoryConstant;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.entity.*;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueLanguageEnum;
import com.xq.enums.DialogueScopeEnum;
import com.xq.enums.StoryCategoryTypeEnum;
import com.xq.mapper.*;
import com.xq.result.PageResult;
import com.xq.service.UserStoryService;
import com.xq.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端 - 剧情浏览实现
 */
@Service
@Slf4j
public class UserStoryServiceImpl implements UserStoryService {

    /**
     * 对话来源：1-剧情
     */
    private static final int SOURCE_TYPE_STORY = 1;
    /**
     * 已发布
     */
    private static final int STORY_STATUS_PUBLISHED = 1;

    @Autowired
    private StoryCategoryMapper storyCategoryMapper;
    @Autowired
    private StoryMapper storyMapper;
    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;
    @Autowired
    private DialogueLineMapper dialogueLineMapper;
    @Autowired
    private DialogueSegmentMapper dialogueSegmentMapper;
    @Autowired
    private DialogueImageMapper dialogueImageMapper;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private StickerMapper stickerMapper;
    @Autowired
    private RoleMapper roleMapper;

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

    @Override
    public PageResult<StoryVO> pageStory(StoryPageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());

        // 强制只查已发布
        query.setStatus(STORY_STATUS_PUBLISHED);
        List<Story> stories = storyMapper.pageQuery(query);
        PageInfo<Story> pageInfo = new PageInfo<>(stories);

        if (stories.isEmpty()) {
            return PageResult.empty(pageInfo.getTotal());
        }

        List<StoryVO> voList = stories.stream()
                .map(this::toStoryVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, pageInfo.getTotal());
    }

    @Override
    public StoryDetailVO storyDetail(Long id) {
        if (id == null) {
            throw new RuntimeException("剧情ID不能为空");
        }
        Story story = storyMapper.getById(id);
        if (story == null) {
            throw new RuntimeException("剧情不存在");
        }

        StoryDetailVO vo = new StoryDetailVO();
        BeanUtils.copyProperties(story, vo);

        // 分类信息
        StoryCategory category = storyCategoryMapper.getById(story.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
            vo.setCategoryType(category.getCategoryType());
            vo.setCategoryTypeLabel(StoryCategoryTypeEnum.getLabel(category.getCategoryType()));
        }

        // 1. 查全部对话版本
        List<DialogueVersion> versions =
                dialogueVersionMapper.listBySource(SOURCE_TYPE_STORY, id);
        if (versions == null || versions.isEmpty()) {
            vo.setVersions(Collections.emptyList());
            return vo;
        }

        List<Long> versionIds = versions.stream()
                .map(DialogueVersion::getId)
                .toList();

        // 2. 批量查所有版本的行、图片，再按 versionId 分组
        List<DialogueLine> allLines = dialogueLineMapper.listByVersionIds(versionIds);
        List<DialogueImage> allImages = dialogueImageMapper.listByVersionIds(versionIds);

        Map<Long, List<DialogueLine>> linesByVersion = allLines.stream()
                .collect(Collectors.groupingBy(DialogueLine::getVersionId));
        Map<Long, List<DialogueImage>> imagesByVersion = allImages.stream()
                .collect(Collectors.groupingBy(DialogueImage::getVersionId));

        // 3. 片段：基于所有 lineIds 批量查
        List<Long> allLineIds = allLines.stream().map(DialogueLine::getId).toList();
        Map<Long, List<DialogueSegment>> segmentsByLine = allLineIds.isEmpty()
                ? Collections.emptyMap()
                : dialogueSegmentMapper.listByLineIds(allLineIds).stream()
                .collect(Collectors.groupingBy(DialogueSegment::getLineId));

        // 4. 说话人：基于所有 speakerId 批量查
        // 4.1 speaker_id -> role
        Set<Long> speakerIds = allLines.stream()
                .map(DialogueLine::getSpeakerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Role> roleMap = speakerIds.isEmpty()
                ? Collections.emptyMap()
                : roleMapper.listByIds(speakerIds).stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        // 4.2 role.personId -> person
        Set<Long> personIds = roleMap.values().stream()
                .map(Role::getPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Person> personMap = personIds.isEmpty()
                ? Collections.emptyMap()
                : personMapper.listByIds(personIds).stream()
                .collect(Collectors.toMap(Person::getId, p -> p));

        // 5. 表情包：基于所有 stickerId 批量查
        Set<Long> stickerIds = segmentsByLine.values().stream()
                .flatMap(List::stream)
                .map(DialogueSegment::getStickerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Sticker> stickerMap = stickerIds.isEmpty()
                ? Collections.emptyMap()
                : stickerMapper.listByIds(stickerIds).stream()
                .collect(Collectors.toMap(Sticker::getId, s -> s));

        // 6. 组装每个版本的 VO
        List<DialogueVersionVO> versionVOs = new ArrayList<>(versions.size());
        for (DialogueVersion version : versions) {
            DialogueVersionVO vvo = new DialogueVersionVO();
            BeanUtils.copyProperties(version, vvo);
            vvo.setLanguageLabel(DialogueLanguageEnum.getLabel(version.getLanguage()));
            vvo.setFormatLabel(DialogueFormatEnum.getLabel(version.getFormat()));
            vvo.setScopeLabel(DialogueScopeEnum.getLabel(version.getScope()));

            // 行
            List<DialogueLine> lines =
                    linesByVersion.getOrDefault(version.getId(), Collections.emptyList());
            List<DialogueLineVO> lineVOs = new ArrayList<>(lines.size());
            for (DialogueLine line : lines) {
                DialogueLineVO lvo = new DialogueLineVO();
                BeanUtils.copyProperties(line, lvo);

                // 通过 role 反查
                Role role = roleMap.get(line.getSpeakerId());
                if (role != null) {
                    lvo.setSpeakerName(role.getName());        // 角色名 → speakerName
                    lvo.setPersonId(role.getPersonId());       // 角色关联的人物ID → personId

                    Person person = personMap.get(role.getPersonId());
                    if (person != null) {
                        lvo.setPersonNameCn(person.getNameCn());  // 人物中文名 → personNameCn
                        lvo.setPersonAvatar(person.getAvatar());  // 人物头像 → personAvatar
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
                            svo.setStickerUrl(sticker.getImageUrl());
                            svo.setStickerEmoji(sticker.getEmoji());
                        }
                    }
                    segVOs.add(svo);
                }
                lvo.setSegments(segVOs);
                lineVOs.add(lvo);
            }
            vvo.setLines(lineVOs);

            // 图片
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

        vo.setVersions(versionVOs);
        return vo;
    }

    // ---------- helpers ----------

    private StoryCategoryVO toCategoryVO(StoryCategory c) {
        if (c == null) return null;
        StoryCategoryVO vo = new StoryCategoryVO();
        BeanUtils.copyProperties(c, vo);
        vo.setCategoryTypeLabel(StoryCategoryTypeEnum.getLabel(c.getCategoryType()));
        return vo;
    }

    private StoryVO toStoryVO(Story s) {
        if (s == null) return null;
        StoryVO vo = new StoryVO();
        BeanUtils.copyProperties(s, vo);
        // 分类信息
        StoryCategory category = storyCategoryMapper.getById(s.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
            vo.setCategoryType(category.getCategoryType());
            vo.setCategoryTypeLabel(StoryCategoryTypeEnum.getLabel(category.getCategoryType()));
        }
        return vo;
    }

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
}
