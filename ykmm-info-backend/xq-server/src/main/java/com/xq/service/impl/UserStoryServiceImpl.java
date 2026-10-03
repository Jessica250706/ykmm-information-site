package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.StoryConstant;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.entity.*;
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
