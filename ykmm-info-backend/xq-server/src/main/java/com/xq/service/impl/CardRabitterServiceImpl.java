package com.xq.service.impl;

import com.xq.dto.CardRabitterDTO;
import com.xq.entity.CardRabitter;
import com.xq.mapper.CardRabitterMapper;
import com.xq.service.CardRabitterService;
import com.xq.vo.CardRabitterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡面 Rabitter 服务实现
 */
@Service
@Slf4j
public class CardRabitterServiceImpl implements CardRabitterService {

    @Autowired
    private CardRabitterMapper cardRabitterMapper;

    /**
     * 查询卡面下的 Rabitter 列表
     *
     * @param cardId 卡面ID
     * @return Rabitter 列表
     */
    @Override
    public List<CardRabitterVO> listByCardId(Long cardId) {
        /** 卡面ID不能为空 */
        if (cardId == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        List<CardRabitter> list = cardRabitterMapper.listByCardId(cardId);
        List<CardRabitterVO> voList = new ArrayList<>(list.size());
        for (CardRabitter rabitter : list) {
            voList.add(toVO(rabitter));
        }
        return voList;
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return Rabitter 详情
     */
    @Override
    public CardRabitterVO getById(Long id) {
        /** Rabitter ID不能为空 */
        if (id == null) {
            throw new RuntimeException("Rabitter ID不能为空");
        }
        CardRabitter rabitter = cardRabitterMapper.getById(id);
        /** Rabitter 必须存在 */
        if (rabitter == null) {
            throw new RuntimeException("Rabitter 不存在");
        }
        return toVO(rabitter);
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @Override
    @Transactional
    public Long create(CardRabitterDTO dto) {
        /** 参数不能为空 */
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        /** 卡面ID不能为空 */
        if (dto.getCardId() == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        /** 话数不能为空 */
        if (dto.getEpisodeNo() == null) {
            throw new RuntimeException("话数不能为空");
        }

        CardRabitter rabitter = new CardRabitter();
        BeanUtils.copyProperties(dto, rabitter);
        cardRabitterMapper.insert(rabitter);
        log.info("新增卡面 Rabitter 成功，id={}", rabitter.getId());
        return rabitter.getId();
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, CardRabitterDTO dto) {
        /** Rabitter ID不能为空 */
        if (id == null) {
            throw new RuntimeException("Rabitter ID不能为空");
        }
        /** 参数不能为空 */
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        CardRabitter exist = cardRabitterMapper.getById(id);
        /** Rabitter 必须存在 */
        if (exist == null) {
            throw new RuntimeException("Rabitter 不存在");
        }
        CardRabitter rabitter = new CardRabitter();
        BeanUtils.copyProperties(dto, rabitter);
        rabitter.setId(id);
        cardRabitterMapper.update(rabitter);
        log.info("编辑卡面 Rabitter 成功，id={}", id);
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        /** Rabitter ID不能为空 */
        if (id == null) {
            throw new RuntimeException("Rabitter ID不能为空");
        }
        CardRabitter exist = cardRabitterMapper.getById(id);
        /** Rabitter 必须存在 */
        if (exist == null) {
            throw new RuntimeException("Rabitter 不存在");
        }
        cardRabitterMapper.deleteById(id);
        log.info("删除卡面 Rabitter 成功，id={}", id);
    }

    /**
     * 根据卡面ID删除
     *
     * @param cardId 卡面ID
     */
    @Override
    @Transactional
    public void deleteByCardId(Long cardId) {
        /** 卡面ID不能为空 */
        if (cardId == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        cardRabitterMapper.deleteByCardId(cardId);
        log.info("根据卡面ID删除 Rabitter 成功，cardId={}", cardId);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 实体 → VO
     *
     * @param rabitter 实体
     * @return VO
     */
    private CardRabitterVO toVO(CardRabitter rabitter) {
        if (rabitter == null) return null;
        CardRabitterVO vo = new CardRabitterVO();
        BeanUtils.copyProperties(rabitter, vo);
        return vo;
    }
}
