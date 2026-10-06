package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.constant.StoryConstant;
import com.xq.context.BaseContext;
import com.xq.dto.StoryAuditDTO;
import com.xq.dto.StoryDTO;
import com.xq.dto.StoryPageQueryDTO;
import com.xq.entity.Story;
import com.xq.entity.StoryCategory;
import com.xq.enums.StoryCategoryTypeEnum;
import com.xq.enums.StatusEnum;
import com.xq.mapper.StoryCategoryMapper;
import com.xq.mapper.StoryMapper;
import com.xq.result.PageResult;
import com.xq.service.StoryService;
import com.xq.vo.StoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 剧情服务实现
 */
@Service
@Slf4j
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryMapper storyMapper;

    @Autowired
    private StoryCategoryMapper storyCategoryMapper;

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<StoryVO> pageQuery(StoryPageQueryDTO query) {
        // PageHelper 自动读取 pageNum / pageSize
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<Story> page = (Page<Story>) storyMapper.pageQuery(query);

        List<StoryVO> voList = new ArrayList<>();
        for (Story story : page.getResult()) {
            voList.add(toVO(story));
        }
        return new PageResult(page.getTotal(), voList);
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 剧情详情
     */
    @Override
    public StoryVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("剧情ID不能为空");
        }
        Story story = storyMapper.getById(id);
        if (story == null) {
            throw new RuntimeException("剧情不存在");
        }
        return toVO(story);
    }

    /**
     * 新增剧情
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @Override
    @Transactional
    public Long create(StoryDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new RuntimeException("话标题不能为空");
        }
        if (dto.getCategoryId() == null) {
            throw new RuntimeException("所属分类不能为空");
        }
        // 分类存在性校验
        StoryCategory category = storyCategoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new RuntimeException("所属分类不存在");
        }

        Story story = new Story();
        BeanUtils.copyProperties(dto, story);
        if (story.getSort() == null) {
            story.setSort(0);
        }
        // 管理端新增默认已发布
        story.setStatus(StatusEnum.PUBLISHED.getValue());
        storyMapper.insert(story);
        log.info("新增剧情成功，id={}", story.getId());
        return story.getId();
    }

    /**
     * 编辑剧情
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, StoryDTO dto) {
        if (id == null) {
            throw new RuntimeException("剧情ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        Story exist = storyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情不存在");
        }
        if (dto.getTitle() != null && dto.getTitle().isBlank()) {
            throw new RuntimeException("话标题不能为空");
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
        storyMapper.update(story);
        log.info("编辑剧情成功，id={}", id);
    }

    /**
     * 删除剧情
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("剧情ID不能为空");
        }
        Story exist = storyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情不存在");
        }
        // 如果是用户提交的待审核内容，后续可考虑逻辑删除
        storyMapper.deleteById(id);
        log.info("删除剧情成功，id={}", id);
    }

    /**
     * 审核剧情
     *
     * @param id         主键
     * @param dto        审核参数
     */
    @Override
    @Transactional
    public void audit(Long id, StoryAuditDTO dto) {
        if (id == null) {
            throw new RuntimeException("剧情ID不能为空");
        }
        if (dto == null || dto.getStatus() == null) {
            throw new RuntimeException("审核结果不能为空");
        }
        // 只允许通过（1）或拒绝（3）
        if (!StatusEnum.PUBLISHED.getValue().equals(dto.getStatus())
                && !StatusEnum.REJECTED.getValue().equals(dto.getStatus())) {
            throw new RuntimeException("审核结果不合法");
        }
        if (dto.getReviewRemark() != null
                && dto.getReviewRemark().length() > StoryConstant.REVIEW_REMARK_MAX_LENGTH) {
            throw new RuntimeException("审核备注长度不能超过 "
                    + StoryConstant.REVIEW_REMARK_MAX_LENGTH);
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
     * 根据分类ID查询剧情列表
     *
     * @param categoryId 分类ID
     * @return 剧情列表
     */
    @Override
    public List<StoryVO> listByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        List<Story> stories = storyMapper.listByCategoryId(categoryId);
        if (stories == null || stories.isEmpty()) {
            return Collections.emptyList();
        }

        // 分类查一次，所有 VO 复用
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
     * Story -> StoryVO
     *
     * @param story 实体
     * @return VO
     */
    private StoryVO toVO(Story story) {
        if (story == null) {
            return null;
        }
        StoryVO vo = new StoryVO();
        BeanUtils.copyProperties(story, vo);
        vo.setStatusLabel(StatusEnum.getLabel(story.getStatus()));

        // 补充分类信息
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
}
