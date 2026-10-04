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

        // 对话版本：有且仅有一个
        List<DialogueVersion> versions =
                dialogueVersionMapper.listBySource(SOURCE_TYPE_STORY, id);
        if (versions == null || versions.isEmpty()) {
            vo.setVersions(Collections.emptyList());
            return vo;
        }
        DialogueVersion version = versions.get(0);
        Long versionId = version.getId();

        // 行、图片：直接单数查询，无需分组
        List<DialogueLine> lines = dialogueLineMapper.listByVersionId(versionId);
        List<DialogueImage> images = dialogueImageMapper.listByVersionId(versionId);

        // 片段：仍需按行批量查（一行多条片段）
        List<Long> lineIds = lines.stream().map(DialogueLine::getId).toList();
        Map<Long, List<DialogueSegment>> segmentsByLine = lineIds.isEmpty()
                ? Collections.emptyMap()
                : dialogueSegmentMapper.listByLineIds(lineIds).stream()
                .collect(Collectors.groupingBy(DialogueSegment::getLineId));

        // 说话人
        Set<Long> speakerIds = lines.stream()
                .map(DialogueLine::getSpeakerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Person> speakerMap = speakerIds.isEmpty()
                ? Collections.emptyMap()
                : personMapper.listByIds(speakerIds).stream()
                .collect(Collectors.toMap(Person::getId, p -> p));

        // 表情包
        Set<Long> stickerIds = segmentsByLine.values().stream()
                .flatMap(List::stream)
                .map(DialogueSegment::getStickerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Sticker> stickerMap = stickerIds.isEmpty()
                ? Collections.emptyMap()
                : stickerMapper.listByIds(stickerIds).stream()
                .collect(Collectors.toMap(Sticker::getId, s -> s));

        // 唯一的版本 VO
        DialogueVersionVO vvo = new DialogueVersionVO();
        BeanUtils.copyProperties(version, vvo);
        vvo.setLanguageLabel(DialogueLanguageEnum.getLabel(version.getLanguage()));
        vvo.setFormatLabel(DialogueFormatEnum.getLabel(version.getFormat()));
        vvo.setScopeLabel(DialogueScopeEnum.getLabel(version.getScope()));

        List<DialogueLineVO> lineVOs = new ArrayList<>(lines.size());
        for (DialogueLine line : lines) {
            DialogueLineVO lvo = new DialogueLineVO();
            BeanUtils.copyProperties(line, lvo);

            Person speaker = speakerMap.get(line.getSpeakerId());
            if (speaker != null) {
                lvo.setSpeakerName(speaker.getNameCn());
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

        List<DialogueImageVO> imageVOs = new ArrayList<>(images.size());
        for (DialogueImage img : images) {
            DialogueImageVO ivo = new DialogueImageVO();
            BeanUtils.copyProperties(img, ivo);
            imageVOs.add(ivo);
        }
        vvo.setImages(imageVOs);

        // 只返回一个版本
        vo.setVersions(List.of(vvo));
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
