package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.dto.CardSeriesDTO;
import com.xq.dto.CardSeriesPageQueryDTO;
import com.xq.entity.CardSeries;
import com.xq.mapper.CardSeriesMapper;
import com.xq.result.PageResult;
import com.xq.service.CardSeriesService;
import com.xq.vo.CardSeriesVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡面系列服务实现
 */
@Service
@Slf4j
public class CardSeriesServiceImpl implements CardSeriesService {

    @Autowired
    private CardSeriesMapper cardSeriesMapper;

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<CardSeriesVO> pageQuery(CardSeriesPageQueryDTO query) {
        if (query == null) {
            throw new RuntimeException("查询条件不能为空");
        }
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        // 借用 searchByName 做分页，PageHelper 会接管
        List<CardSeries> list = cardSeriesMapper.searchByName(query.getKeyword());
        PageInfo<CardSeries> page = new PageInfo<>(list);

        List<CardSeriesVO> voList = new ArrayList<>(page.getList().size());
        for (CardSeries s : page.getList()) {
            voList.add(toVO(s, true));
        }
        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 查询全部
     *
     * @return 系列列表
     */
    @Override
    public List<CardSeriesVO> listAll() {
        List<CardSeries> list = cardSeriesMapper.listAll();
        List<CardSeriesVO> voList = new ArrayList<>(list.size());
        for (CardSeries s : list) {
            voList.add(toVO(s, false));
        }
        return voList;
    }

    /**
     * 按名称搜索
     *
     * @param keyword 关键字
     * @return 系列列表
     */
    @Override
    public List<CardSeriesVO> searchByName(String keyword) {
        List<CardSeries> list = cardSeriesMapper.searchByName(keyword);
        List<CardSeriesVO> voList = new ArrayList<>(list.size());
        for (CardSeries s : list) {
            voList.add(toVO(s, false));
        }
        return voList;
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 系列详情
     */
    @Override
    public CardSeriesVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("系列ID不能为空");
        }
        CardSeries s = cardSeriesMapper.getById(id);
        if (s == null) {
            throw new RuntimeException("系列不存在");
        }
        return toVO(s, true);
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @Override
    @Transactional
    public Long create(CardSeriesDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("系列名不能为空");
        }
        // 名称唯一校验
        List<CardSeries> exists = cardSeriesMapper.searchByName(dto.getName());
        for (CardSeries s : exists) {
            if (dto.getName().equals(s.getName())) {
                throw new RuntimeException("系列名已存在");
            }
        }

        CardSeries series = new CardSeries();
        BeanUtils.copyProperties(dto, series);
        cardSeriesMapper.insert(series);
        log.info("新增卡面系列成功，id={}", series.getId());
        return series.getId();
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, CardSeriesDTO dto) {
        if (id == null) {
            throw new RuntimeException("系列ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        CardSeries exist = cardSeriesMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("系列不存在");
        }
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new RuntimeException("系列名不能为空");
            }
            List<CardSeries> others = cardSeriesMapper.searchByName(dto.getName());
            for (CardSeries s : others) {
                if (dto.getName().equals(s.getName()) && !s.getId().equals(id)) {
                    throw new RuntimeException("系列名已存在");
                }
            }
        }

        CardSeries series = new CardSeries();
        BeanUtils.copyProperties(dto, series);
        series.setId(id);
        cardSeriesMapper.update(series);
        log.info("编辑卡面系列成功，id={}", id);
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("系列ID不能为空");
        }
        CardSeries exist = cardSeriesMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("系列不存在");
        }
        int count = cardSeriesMapper.countCards(id);
        if (count > 0) {
            throw new RuntimeException("该系列下还有 " + count + " 张卡面，无法删除");
        }
        cardSeriesMapper.deleteById(id);
        log.info("删除卡面系列成功，id={}", id);
    }

    /**
     * 按名称查询或创建
     *
     * @param name 系列名
     * @return 系列ID
     */
    @Override
    @Transactional
    public Long findOrCreateByName(String name) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("系列名不能为空");
        }
        List<CardSeries> list = cardSeriesMapper.searchByName(name);
        for (CardSeries s : list) {
            if (name.equals(s.getName())) {
                return s.getId();
            }
        }
        CardSeries series = new CardSeries();
        series.setName(name);
        cardSeriesMapper.insert(series);
        log.info("按名称自动创建卡面系列成功，id={}, name={}", series.getId(), name);
        return series.getId();
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 实体 → VO
     *
     * @param series       实体
     * @param withCardCount 是否统计关联卡面数量
     */
    private CardSeriesVO toVO(CardSeries series, boolean withCardCount) {
        if (series == null) return null;
        CardSeriesVO vo = new CardSeriesVO();
        BeanUtils.copyProperties(series, vo);
        if (withCardCount) {
            vo.setCardCount(cardSeriesMapper.countCards(series.getId()));
        }
        return vo;
    }
}
