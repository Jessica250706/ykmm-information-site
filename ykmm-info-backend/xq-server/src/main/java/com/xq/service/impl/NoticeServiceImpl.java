package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.NoticeStatusConstant;
import com.xq.constant.NoticeVisibilityConstant;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.NoticeDTO;
import com.xq.dto.NoticePageQueryDTO;
import com.xq.entity.SysNotice;
import com.xq.entity.SysNoticeCategory;
import com.xq.entity.SysUser;
import com.xq.enums.NoticeStatusEnum;
import com.xq.enums.NoticeVisibilityEnum;
import com.xq.mapper.NoticeCategoryMapper;
import com.xq.mapper.NoticeMapper;
import com.xq.mapper.UserMapper;
import com.xq.result.PageResult;
import com.xq.service.NoticeService;
import com.xq.vo.NoticeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公告服务实现
 */
@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private NoticeCategoryMapper noticeCategoryMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<NoticeVO> pageVisible(NoticePageQueryDTO query) {
        int currentVisibility = currentVisibility();

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<SysNotice> list = noticeMapper.pageVisible(query, currentVisibility);
        PageInfo<SysNotice> page = new PageInfo<>(list);

        List<NoticeVO> voList = assembleVOs(page.getList());
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public NoticeVO detail(Long id) {
        if (id == null) throw new RuntimeException("公告ID不能为空");

        SysNotice notice = noticeMapper.getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }

        // 非管理员：必须通过状态 + 可见性 + 时间校验
        if (!RoleContext.isAdmin()) {
            if (!NoticeStatusEnum.PUBLISHED.getValue().equals(notice.getStatus())) {
                throw new RuntimeException("公告不存在");
            }
            if (notice.getMinVisibility() != null
                    && notice.getMinVisibility() > currentVisibility()) {
                throw new RuntimeException("公告不存在");
            }
            LocalDateTime now = LocalDateTime.now();
            if (notice.getPublishAt() != null && notice.getPublishAt().isAfter(now)) {
                throw new RuntimeException("公告不存在");
            }
            if (notice.getExpireAt() != null && notice.getExpireAt().isBefore(now)) {
                throw new RuntimeException("公告不存在");
            }

            // 非管理员访问才累计浏览量
            noticeMapper.incrViewCount(id);
            notice.setViewCount(notice.getViewCount() == null ? 1 : notice.getViewCount() + 1);
        }

        return assembleVOs(List.of(notice)).get(0);
    }

    @Override
    public PageResult<NoticeVO> pageAdmin(NoticePageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<SysNotice> list = noticeMapper.pageAdmin(query);
        PageInfo<SysNotice> page = new PageInfo<>(list);

        List<NoticeVO> voList = assembleVOs(page.getList());
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public NoticeVO adminDetail(Long id) {
        if (id == null) throw new RuntimeException("公告ID不能为空");
        SysNotice notice = noticeMapper.getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        return assembleVOs(List.of(notice)).get(0);
    }

    @Override
    @Transactional
    public Long create(NoticeDTO dto) {
        if (dto == null) throw new RuntimeException("参数不能为空");
        validateVisibility(dto.getMinVisibility());

        SysNoticeCategory category = noticeCategoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new RuntimeException("公告分类不存在：id=" + dto.getCategoryId());
        }

        SysNotice notice = new SysNotice();
        BeanUtils.copyProperties(dto, notice);
        // 新增一律为草稿，由 publish 接口触发发布
        notice.setStatus(NoticeStatusConstant.DRAFT);
        notice.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        notice.setViewCount(0);
        notice.setCreatorId(BaseContext.getCurrentId());

        noticeMapper.insert(notice);
        log.info("新增公告成功，id={}, creatorId={}", notice.getId(), notice.getCreatorId());
        return notice.getId();
    }

    @Override
    @Transactional
    public void update(Long id, NoticeDTO dto) {
        if (id == null) throw new RuntimeException("公告ID不能为空");
        if (dto == null) throw new RuntimeException("参数不能为空");

        SysNotice exist = noticeMapper.getById(id);
        if (exist == null) throw new RuntimeException("公告不存在");

        validateVisibility(dto.getMinVisibility());

        SysNoticeCategory category = noticeCategoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new RuntimeException("公告分类不存在：id=" + dto.getCategoryId());
        }

        SysNotice update = new SysNotice();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);

        noticeMapper.update(update);
        log.info("编辑公告成功，id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new RuntimeException("公告ID不能为空");
        SysNotice exist = noticeMapper.getById(id);
        if (exist == null) throw new RuntimeException("公告不存在");

        noticeMapper.deleteById(id);
        log.info("删除公告成功，id={}", id);
    }

    @Override
    @Transactional
    public void publish(Long id) {
        if (id == null) throw new RuntimeException("公告ID不能为空");
        SysNotice exist = noticeMapper.getById(id);
        if (exist == null) throw new RuntimeException("公告不存在");

        if (NoticeStatusEnum.PUBLISHED.getValue().equals(exist.getStatus())) {
            throw new RuntimeException("公告已是发布状态");
        }

        SysNotice update = new SysNotice();
        update.setId(id);
        update.setStatus(NoticeStatusEnum.PUBLISHED.getValue());
        // publishAt 为空时置为当前时间（保留用户预设的未来时间为定时发布）
        if (exist.getPublishAt() == null) {
            update.setPublishAt(LocalDateTime.now());
        }

        noticeMapper.update(update);
        log.info("发布公告成功，id={}", id);
    }

    @Override
    @Transactional
    public void offline(Long id) {
        if (id == null) throw new RuntimeException("公告ID不能为空");
        SysNotice exist = noticeMapper.getById(id);
        if (exist == null) throw new RuntimeException("公告不存在");

        if (!NoticeStatusEnum.PUBLISHED.getValue().equals(exist.getStatus())) {
            throw new RuntimeException("只有已发布状态才能下线");
        }

        SysNotice update = new SysNotice();
        update.setId(id);
        update.setStatus(NoticeStatusEnum.OFFLINE.getValue());

        noticeMapper.update(update);
        log.info("下线公告成功，id={}", id);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 计算当前访问者的可见级别
     */
    private int currentVisibility() {
        if (RoleContext.isAdmin()) return NoticeVisibilityConstant.ADMIN;
        if (RoleContext.isGuest()) return NoticeVisibilityConstant.GUEST;
        return NoticeVisibilityConstant.USER;
    }

    private void validateVisibility(Integer visibility) {
        if (!NoticeVisibilityEnum.isValid(visibility)) {
            throw new RuntimeException("可见性参数不合法：" + visibility);
        }
    }

    /**
     * 批量组装 VO（含分类名、分类颜色、创建者昵称）
     */
    private List<NoticeVO> assembleVOs(List<SysNotice> notices) {
        if (notices == null || notices.isEmpty()) return Collections.emptyList();

        // 分类字典（数量少，全量加载后内存匹配）
        Map<Integer, SysNoticeCategory> categoryMap = noticeCategoryMapper.listAll().stream()
                .collect(Collectors.toMap(SysNoticeCategory::getId, c -> c, (a, b) -> a));

        // 创建者（按 userId 去重后逐个查；实际使用中去重后通常不超过 2 个）
        Map<Long, SysUser> creatorMap = new HashMap<>();
        notices.stream()
                .map(SysNotice::getCreatorId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(cid -> {
                    SysUser u = userMapper.getById(cid);
                    if (u != null) creatorMap.put(cid, u);
                });

        List<NoticeVO> result = new ArrayList<>(notices.size());
        for (SysNotice notice : notices) {
            NoticeVO vo = new NoticeVO();
            BeanUtils.copyProperties(notice, vo);

            vo.setStatusLabel(NoticeStatusEnum.getLabel(notice.getStatus()));
            vo.setMinVisibilityLabel(NoticeVisibilityEnum.getLabel(notice.getMinVisibility()));

            SysNoticeCategory category = categoryMap.get(notice.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryColor(category.getColor());
            }

            SysUser creator = creatorMap.get(notice.getCreatorId());
            if (creator != null) {
                vo.setCreatorName(
                        creator.getNickname() != null && !creator.getNickname().isBlank()
                                ? creator.getNickname()
                                : creator.getEmail());
            }

            result.add(vo);
        }
        return result;
    }
}
