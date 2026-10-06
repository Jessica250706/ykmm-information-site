package com.xq.service.impl;

import com.xq.dto.CardRtvDTO;
import com.xq.entity.CardRtv;
import com.xq.mapper.CardRtvMapper;
import com.xq.service.CardRtvService;
import com.xq.vo.CardRtvVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡面 RTV 服务实现
 */
@Service
@Slf4j
public class CardRtvServiceImpl implements CardRtvService {

    @Autowired
    private CardRtvMapper cardRtvMapper;

    /**
     * 查询卡面下的 RTV 列表
     *
     * @param cardId 卡面ID
     * @return RTV 列表
     */
    @Override
    public List<CardRtvVO> listByCardId(Long cardId) {
        if (cardId == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        List<CardRtv> list = cardRtvMapper.listByCardId(cardId);
        List<CardRtvVO> voList = new ArrayList<>(list.size());
        for (CardRtv rtv : list) {
            voList.add(toVO(rtv));
        }
        return voList;
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return RTV 详情
     */
    @Override
    public CardRtvVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("RTV ID不能为空");
        }
        CardRtv rtv = cardRtvMapper.getById(id);
        if (rtv == null) {
            throw new RuntimeException("RTV 不存在");
        }
        return toVO(rtv);
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @Override
    @Transactional
    public Long create(CardRtvDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getCardId() == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        if (dto.getEpisodeNo() == null) {
            throw new RuntimeException("话数不能为空");
        }

        CardRtv rtv = new CardRtv();
        BeanUtils.copyProperties(dto, rtv);
        cardRtvMapper.insert(rtv);
        log.info("新增卡面 RTV 成功，id={}", rtv.getId());
        return rtv.getId();
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, CardRtvDTO dto) {
        if (id == null) {
            throw new RuntimeException("RTV ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        CardRtv exist = cardRtvMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("RTV 不存在");
        }
        CardRtv rtv = new CardRtv();
        BeanUtils.copyProperties(dto, rtv);
        rtv.setId(id);
        cardRtvMapper.update(rtv);
        log.info("编辑卡面 RTV 成功，id={}", id);
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
            throw new RuntimeException("RTV ID不能为空");
        }
        CardRtv exist = cardRtvMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("RTV 不存在");
        }
        cardRtvMapper.deleteById(id);
        log.info("删除卡面 RTV 成功，id={}", id);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 实体 → VO
     *
     * @param rtv 实体
     * @return VO
     */
    private CardRtvVO toVO(CardRtv rtv) {
        if (rtv == null) return null;
        CardRtvVO vo = new CardRtvVO();
        BeanUtils.copyProperties(rtv, vo);
        return vo;
    }
}
