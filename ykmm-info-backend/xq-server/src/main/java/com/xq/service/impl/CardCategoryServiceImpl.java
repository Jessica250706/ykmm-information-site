package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.CardCategoryDTO;
import com.xq.dto.CardCategoryPageQueryDTO;
import com.xq.entity.CardCategory;
import com.xq.exception.BusinessException;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.mapper.CardCategoryMapper;
import com.xq.result.PageResult;
import com.xq.service.CardCategoryService;
import com.xq.vo.CardCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 卡面类别服务实现
 */
@Service
@Slf4j
public class CardCategoryServiceImpl implements CardCategoryService {

    /** 默认排序值 */
    private static final int DEFAULT_SORT = 0;

    @Autowired
    private CardCategoryMapper cardCategoryMapper;

    /**
     * 分页查询
     */
    @Override
    public PageResult<CardCategoryVO> pageQuery(CardCategoryPageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<CardCategoryVO> list = cardCategoryMapper.pageQuery(query);
        PageInfo<CardCategoryVO> page = new PageInfo<>(list);
        return new PageResult<>(page.getTotal(), page.getList());
    }

    /**
     * 查询全部（下拉用）
     */
    @Override
    public List<CardCategoryVO> listAll() {
        return convert(cardCategoryMapper.listAll());
    }

    /**
     * 查询详情
     */
    @Override
    public CardCategoryVO getById(Integer id) {
        CardCategory category = cardCategoryMapper.getById(id);
        if (category == null) {
            throw new BusinessException("卡面类别不存在");
        }
        return toVO(category);
    }

    /**
     * 新增
     */
    @Override
    @Transactional
    public Integer create(CardCategoryDTO dto) {
        // 权限
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能新增卡面类别");
        }

        // 名称唯一
        int dup = cardCategoryMapper.countByName(dto.getName(), null);
        if (dup > 0) {
            throw new BusinessException("卡面类别「" + dto.getName() + "」已存在");
        }

        CardCategory category = new CardCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getSort() == null) {
            category.setSort(DEFAULT_SORT);
        }
        cardCategoryMapper.insert(category);

        log.info("新增卡面类别成功，id={}, name={}, 操作人={}",
                category.getId(), category.getName(), BaseContext.getCurrentId());
        return category.getId();
    }

    /**
     * 编辑
     */
    @Override
    @Transactional
    public void update(Integer id, CardCategoryDTO dto) {
        // 权限
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能修改卡面类别");
        }

        if (id == null) {
            throw new BusinessException("类别ID不能为空");
        }

        CardCategory exist = cardCategoryMapper.getById(id);
        if (exist == null) {
            throw new BusinessException("卡面类别不存在");
        }

        // 名称唯一（排除自己）
        String effectiveName = dto.getName() != null ? dto.getName() : exist.getName();
        int dup = cardCategoryMapper.countByName(effectiveName, id);
        if (dup > 0) {
            throw new BusinessException("卡面类别「" + effectiveName + "」已存在");
        }

        CardCategory update = new CardCategory();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        cardCategoryMapper.update(update);

        log.info("编辑卡面类别成功，id={}, 操作人={}", id, BaseContext.getCurrentId());
    }

    /**
     * 删除
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        // 权限
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能删除卡面类别");
        }

        if (id == null) {
            throw new BusinessException("类别ID不能为空");
        }

        CardCategory exist = cardCategoryMapper.getById(id);
        if (exist == null) {
            throw new BusinessException("卡面类别不存在");
        }

        // 引用检查：被 card 引用时禁止删除
        int referenced = cardCategoryMapper.countCardsByCategoryId(id);
        if (referenced > 0) {
            throw new BusinessException("该类别下还有 " + referenced + " 张卡面，无法删除");
        }

        cardCategoryMapper.deleteById(id);
        log.info("删除卡面类别成功，id={}, 操作人={}", id, BaseContext.getCurrentId());
    }

    /* ---------------- 私有方法 ---------------- */

    private CardCategoryVO toVO(CardCategory category) {
        if (category == null) return null;
        CardCategoryVO vo = new CardCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    private List<CardCategoryVO> convert(List<CardCategory> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
}
