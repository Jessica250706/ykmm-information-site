package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.context.BaseContext;
import com.xq.dto.CardDTO;
import com.xq.dto.CardImageDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.entity.Card;
import com.xq.entity.CardImage;
import com.xq.enums.*;
import com.xq.mapper.CardImageMapper;
import com.xq.mapper.CardMapper;
import com.xq.mapper.CardPersonRelMapper;
import com.xq.mapper.CardSeriesMapper;
import com.xq.result.PageResult;
import com.xq.service.CardService;
import com.xq.vo.CardImageVO;
import com.xq.vo.CardPersonVO;
import com.xq.vo.CardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡面服务实现
 */
@Service
@Slf4j
public class CardServiceImpl implements CardService {

    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private CardImageMapper cardImageMapper;
    @Autowired
    private CardPersonRelMapper cardPersonRelMapper;
    @Autowired
    private CardSeriesMapper cardSeriesMapper;

    /**
     * 分页查询
     */
    @Override
    public PageResult<CardVO> pageQuery(CardPageQueryDTO query) {
        if (query == null) {
            throw new RuntimeException("查询条件不能为空");
        }
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Card> list = cardMapper.pageQuery(query);
        PageInfo<Card> page = new PageInfo<>(list);

        List<CardVO> voList = new ArrayList<>(page.getList().size());
        for (Card card : page.getList()) {
            voList.add(toVO(card, false));
        }
        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 查询详情
     */
    @Override
    public CardVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        Card card = cardMapper.getById(id);
        if (card == null) {
            throw new RuntimeException("卡面不存在");
        }
        return toVO(card, true);
    }

    /**
     * 新增
     */
    @Override
    @Transactional
    public Long create(CardDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("卡面名称不能为空");
        }
        if (!CardMaxRarityEnum.isValid(dto.getMaxRarity())) {
            throw new RuntimeException("卡面最高等级不合法");
        }
        if (dto.getAttribute() != null && !CardAttributeEnum.isValid(dto.getAttribute())) {
            throw new RuntimeException("属性不合法");
        }
        if (dto.getAttachedStoryType() != null
                && !CardAttachedStoryTypeEnum.isValid(dto.getAttachedStoryType())) {
            throw new RuntimeException("附属剧情类型不合法");
        }
        if (dto.getCostumeType() != null && !CardCostumeTypeEnum.isValid(dto.getCostumeType())) {
            throw new RuntimeException("服装类型不合法");
        }
        if (cardMapper.countByName(dto.getName(), null) > 0) {
            throw new RuntimeException("卡面名称已存在");
        }

        Card card = new Card();
        BeanUtils.copyProperties(dto, card);
        card.setStatus(StatusEnum.PUBLISHED.getValue());
        card.setCreatorId(BaseContext.getCurrentId());
        cardMapper.insert(card);
        Long cardId = card.getId();

        saveImages(cardId, dto.getImages());
        savePersonRels(cardId, dto.getPersonIds());

        log.info("新增卡面成功，id={}", cardId);
        return cardId;
    }

    /**
     * 编辑
     */
    @Override
    @Transactional
    public void update(Long id, CardDTO dto) {
        if (id == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        Card exist = cardMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("卡面不存在");
        }
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new RuntimeException("卡面名称不能为空");
            }
            if (cardMapper.countByName(dto.getName(), id) > 0) {
                throw new RuntimeException("卡面名称已存在");
            }
        }
        if (dto.getMaxRarity() != null && !CardMaxRarityEnum.isValid(dto.getMaxRarity())) {
            throw new RuntimeException("卡面最高等级不合法");
        }
        if (dto.getAttribute() != null && !CardAttributeEnum.isValid(dto.getAttribute())) {
            throw new RuntimeException("属性不合法");
        }

        Card card = new Card();
        BeanUtils.copyProperties(dto, card);
        card.setId(id);
        cardMapper.update(card);

        // 图片：全删全插
        if (dto.getImages() != null) {
            cardImageMapper.deleteByCardId(id);
            saveImages(id, dto.getImages());
        }
        // 人物关联：全删全插
        if (dto.getPersonIds() != null) {
            cardPersonRelMapper.deleteByCardId(id);
            savePersonRels(id, dto.getPersonIds());
        }

        log.info("编辑卡面成功，id={}", id);
    }

    /**
     * 删除
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        Card exist = cardMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("卡面不存在");
        }
        cardMapper.deleteById(id);
        cardImageMapper.deleteByCardId(id);
        cardPersonRelMapper.deleteByCardId(id);
        log.info("删除卡面成功，id={}", id);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 保存卡面图片
     */
    private void saveImages(Long cardId, List<CardImageDTO> images) {
        if (images == null || images.isEmpty()) return;
        List<CardImage> list = new ArrayList<>(images.size());
        for (int i = 0; i < images.size(); i++) {
            CardImageDTO d = images.get(i);
            if (d.getUrl() == null || d.getUrl().isBlank()) continue;
            if (!CardImageTypeEnum.isValid(d.getImageType())) {
                throw new RuntimeException("图片类型不合法：" + d.getImageType());
            }
            CardImage img = new CardImage();
            img.setCardId(cardId);
            img.setImageType(d.getImageType());
            img.setUrl(d.getUrl());
            img.setSort(d.getSort() != null ? d.getSort() : i + 1);
            list.add(img);
        }
        if (!list.isEmpty()) cardImageMapper.insertBatch(list);
    }

    /**
     * 保存人物关联
     */
    private void savePersonRels(Long cardId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) return;
        cardPersonRelMapper.insertBatch(cardId, personIds);
    }

    /**
     * 实体 → VO
     *
     * @param card       卡面实体
     * @param withDetail 是否加载图片和人物
     */
    private CardVO toVO(Card card, boolean withDetail) {
        if (card == null) return null;
        CardVO vo = new CardVO();
        BeanUtils.copyProperties(card, vo);
        vo.setMaxRarityLabel(CardMaxRarityEnum.getLabel(card.getMaxRarity()));
        vo.setAttributeLabel(CardAttributeEnum.getLabel(card.getAttribute()));
        vo.setAttachedStoryTypeLabel(CardAttachedStoryTypeEnum.getLabel(card.getAttachedStoryType()));
        vo.setCostumeTypeLabel(CardCostumeTypeEnum.getLabel(card.getCostumeType()));

        // 系列名
        if (card.getSeriesId() != null) {
            var series = cardSeriesMapper.getById(card.getSeriesId());
            if (series != null) vo.setSeriesName(series.getName());
        }

        if (withDetail) {
            // 图片
            List<CardImage> imgs = cardImageMapper.listByCardId(card.getId());
            List<CardImageVO> imgVOs = new ArrayList<>(imgs.size());
            for (CardImage img : imgs) {
                CardImageVO ivo = new CardImageVO();
                BeanUtils.copyProperties(img, ivo);
                ivo.setImageTypeLabel(CardImageTypeEnum.getLabel(img.getImageType()));
                imgVOs.add(ivo);
            }
            vo.setImages(imgVOs);

            // 人物
            List<CardPersonVO> persons = cardPersonRelMapper.listPersonsByCardId(card.getId());
            vo.setPersons(persons);
        }
        return vo;
    }
}
