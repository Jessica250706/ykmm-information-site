package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.CardDTO;
import com.xq.dto.CardImageDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.entity.*;
import com.xq.enums.*;
import com.xq.mapper.*;
import com.xq.result.PageResult;
import com.xq.service.CardService;
import com.xq.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private CardRcMapper cardRcMapper;
    @Autowired
    private CardRtvMapper cardRtvMapper;
    @Autowired
    private CardRabitterMapper cardRabitterMapper;
    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;
    @Autowired
    private DialogueLineMapper dialogueLineMapper;
    @Autowired
    private DialogueSegmentMapper dialogueSegmentMapper;
    @Autowired
    private DialogueImageMapper dialogueImageMapper;
    @Autowired
    private RcOptionMapper rcOptionMapper;
    @Autowired
    private StickerMapper stickerMapper;
    @Autowired
    private DialogueVersionContributorMapper dialogueVersionContributorMapper;

    /**
     * 分页查询
     */
    @Override
    public PageResult<CardVO> pageQuery(CardPageQueryDTO query) {
        if (query == null) {
            throw new RuntimeException("查询条件不能为空");
        }

        // 强制只查已发布
        RoleContext.restrictToPublishedIfUser(query::setStatus);

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Card> list = cardMapper.pageQuery(query);
        PageInfo<Card> page = new PageInfo<>(list);

        if (page.getList().isEmpty()) {
            return new PageResult<>(page.getTotal(), Collections.emptyList());
        }

        // 收集本页所有 cardId
        List<Long> cardIds = page.getList().stream()
                .map(Card::getId)
                .filter(Objects::nonNull)
                .toList();

        // 批量查图片、人物
        Map<Long, List<CardImage>> imageMap = cardIds.isEmpty()
                ? Collections.emptyMap()
                : cardImageMapper.listByCardIds(cardIds).stream()
                .collect(Collectors.groupingBy(CardImage::getCardId));

        Map<Long, List<CardPersonVO>> personMap = cardIds.isEmpty()
                ? Collections.emptyMap()
                : cardPersonRelMapper.listPersonsByCardIds(cardIds).stream()
                .collect(Collectors.groupingBy(CardPersonVO::getCardId));

        // 组装
        List<CardVO> voList = new ArrayList<>(page.getList().size());
        for (Card card : page.getList()) {
            CardVO vo = toVO(card, false);

            // 图片
            List<CardImage> imgs = imageMap.getOrDefault(card.getId(), Collections.emptyList());
            List<CardImageVO> imgVOs = new ArrayList<>(imgs.size());
            for (CardImage img : imgs) {
                CardImageVO ivo = new CardImageVO();
                BeanUtils.copyProperties(img, ivo);
                ivo.setImageTypeLabel(CardImageTypeEnum.getLabel(img.getImageType()));
                imgVOs.add(ivo);
            }
            vo.setImages(imgVOs);

            // 人物
            List<CardPersonVO> persons =
                    personMap.getOrDefault(card.getId(), Collections.emptyList());
            vo.setPersons(new ArrayList<>(persons));

            voList.add(vo);
        }
        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 查询详情（含附属剧情对话）
     *
     * @param id 卡面ID
     * @return 卡面详情
     */
    @Override
    public CardVO getById(Long id) {
        /** 卡面ID不能为空 */
        if (id == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        Card card = cardMapper.getById(id);
        /** 卡面必须存在 */
        if (card == null) {
            throw new RuntimeException("卡面不存在");
        }

        if (RoleContext.isUser()) {
            /** 用户端只允许查看已发布卡面 */
            if (!StatusEnum.PUBLISHED.getValue().equals(card.getStatus())) {
                throw new RuntimeException("卡面不存在");
            }
        }

        CardVO vo = toVO(card, true);

        /** 装配附属剧情 */
        vo.setAttachedStory(buildAttachedStory(card));

        return vo;
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
                && !DialogueSourceTypeEnum.isValid(dto.getAttachedStoryType())) {
            throw new RuntimeException("附属剧情类型不合法");
        }
        if (dto.getCostumeType() != null && !CardCostumeTypeEnum.isValid(dto.getCostumeType())) {
            throw new RuntimeException("服装类型不合法");
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
        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new RuntimeException("卡面名称不能为空");
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
        vo.setAttachedStoryTypeLabel(DialogueSourceTypeEnum.getLabel(card.getAttachedStoryType()));
        vo.setCostumeTypeLabel(CardCostumeTypeEnum.getLabel(card.getCostumeType()));

        // 保证列表字段非 null
        vo.setImages(new ArrayList<>());
        vo.setPersons(new ArrayList<>());

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
            vo.setPersons(cardPersonRelMapper.listPersonsByCardId(card.getId()));
        }
        return vo;
    }

    /**
     * 装配卡面附属剧情
     *
     * @param card 卡面实体
     * @return 附属剧情VO，无附属剧情返回 null
     */
    private CardAttachedStoryVO buildAttachedStory(Card card) {
        Integer type = card.getAttachedStoryType();
        /** 无附属剧情 */
        if (type == null || type.equals(DialogueSourceTypeEnum.NONE.getValue())) {
            return null;
        }

        CardAttachedStoryVO storyVO = new CardAttachedStoryVO();
        storyVO.setStoryType(type);
        storyVO.setStoryTypeLabel(DialogueSourceTypeEnum.getLabel(type));

        List<CardEpisodeVO> episodes = new ArrayList<>();
        storyVO.setEpisodes(episodes);

        Long cardId = card.getId();
        Integer statusFilter = RoleContext.isUser() ? StatusEnum.PUBLISHED.getValue() : null;

        /** 先按附属剧情类型拿到话列表 */
        if (type.equals(DialogueSourceTypeEnum.RC.getValue())) {
            // RC
            List<CardRcVO> rcList = cardRcMapper.listVOByCardId(cardId);
            for (CardRcVO rc : rcList) {
                CardEpisodeVO ep = new CardEpisodeVO();
                ep.setId(rc.getId());
                ep.setCardId(rc.getCardId());
                ep.setEpisodeNo(rc.getEpisodeNo());
                ep.setTitle(rc.getTitle());
                ep.setInitiatorRoleId(rc.getRoleId());
                ep.setInitiatorRoleName(rc.getRoleName());
                ep.setVersions(new ArrayList<>());
                episodes.add(ep);
            }
        } else if (type.equals(DialogueSourceTypeEnum.RTV.getValue())) {
            // RTV
            List<CardRtv> rtvList = cardRtvMapper.listByCardId(cardId);
            for (CardRtv rtv : rtvList) {
                CardEpisodeVO ep = new CardEpisodeVO();
                ep.setId(rtv.getId());
                ep.setCardId(rtv.getCardId());
                ep.setEpisodeNo(rtv.getEpisodeNo());
                ep.setTitle(rtv.getTitle());
                ep.setVersions(new ArrayList<>());
                episodes.add(ep);
            }
        } else if (type.equals(DialogueSourceTypeEnum.RABITTER.getValue())) {
            List<CardRabitter> rabitterListList = cardRabitterMapper.listByCardId(cardId);
            for (CardRabitter rabitter : rabitterListList) {
                CardEpisodeVO ep = new CardEpisodeVO();
                ep.setId(rabitter.getId());
                ep.setCardId(rabitter.getCardId());
                ep.setEpisodeNo(rabitter.getEpisodeNo());
                ep.setTitle(rabitter.getTitle());
                ep.setVersions(new ArrayList<>());
                episodes.add(ep);
            }
            log.warn("Rabbitter 附属剧情暂无对应表，cardId={}", cardId);
            return storyVO;
        } else {
            return storyVO;
        }

        if (episodes.isEmpty()) {
            return storyVO;
        }

        List<Long> sourceIds = episodes.stream()
                .map(CardEpisodeVO::getId)
                .filter(Objects::nonNull)
                .toList();
        if (sourceIds.isEmpty()) {
            return storyVO;
        }

        /** 批量查询对话版本 */
        List<DialogueVersion> versions =
                dialogueVersionMapper.listBySourceIds(type, sourceIds, statusFilter);
        if (versions.isEmpty()) {
            return storyVO;
        }

        List<Long> versionIds = versions.stream()
                .map(DialogueVersion::getId)
                .toList();

        /** 批量查询对话句子 */
        List<DialogueLine> lines = versionIds.isEmpty()
                ? Collections.emptyList()
                : dialogueLineMapper.listByVersionIds(versionIds);

        Map<Long, List<DialogueLine>> linesByVersion = lines.stream()
                .collect(Collectors.groupingBy(DialogueLine::getVersionId));

        /** 批量查询片段 */
        List<Long> lineIds = lines.stream().map(DialogueLine::getId).toList();
        List<DialogueSegment> segments = lineIds.isEmpty()
                ? Collections.emptyList()
                : dialogueSegmentMapper.listByLineIds(lineIds);

        Map<Long, List<DialogueSegment>> segmentsByLine = segments.stream()
                .collect(Collectors.groupingBy(DialogueSegment::getLineId));

        /** 批量查询表情包 */
        Set<Long> stickerIds = segments.stream()
                .map(DialogueSegment::getStickerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Sticker> stickerMap;
        if (stickerIds.isEmpty()) {
            stickerMap = Collections.emptyMap();
        } else {
            stickerMap = stickerMapper.listByIds(new ArrayList<>(stickerIds)).stream()
                    .collect(Collectors.toMap(Sticker::getId, s -> s, (a, b) -> a));
        }

        /** 批量查询图片版本 */
        List<DialogueImage> images = versionIds.isEmpty()
                ? Collections.emptyList()
                : dialogueImageMapper.listByVersionIds(versionIds);

        Map<Long, List<DialogueImage>> imagesByVersion = images.stream()
                .collect(Collectors.groupingBy(DialogueImage::getVersionId));

        /** 批量查询 RC 选项 */
        Map<Long, List<RcOption>> optionsByVersion;
        if (type == 1) {
            List<RcOption> options = versionIds.isEmpty()
                    ? Collections.emptyList()
                    : rcOptionMapper.listByVersionIds(versionIds);
            optionsByVersion = options.stream()
                    .collect(Collectors.groupingBy(RcOption::getVersionId));
        } else {
            optionsByVersion = Collections.emptyMap();
        }

        /** 批量查询贡献者 */
        List<DialogueVersionContributorVO> contributors = versionIds.isEmpty()
                ? Collections.emptyList()
                : dialogueVersionContributorMapper.listByVersionIds(versionIds);

        Map<Long, List<DialogueVersionContributorVO>> contributorsByVersion = contributors.stream()
                .collect(Collectors.groupingBy(DialogueVersionContributorVO::getVersionId));

        /** 按 source_id 分组版本 */
        Map<Long, List<DialogueVersion>> versionsBySource = versions.stream()
                .collect(Collectors.groupingBy(DialogueVersion::getSourceId));

        /** 组装 */
        for (CardEpisodeVO ep : episodes) {
            List<DialogueVersion> epVersions =
                    versionsBySource.getOrDefault(ep.getId(), Collections.emptyList());
            List<DialogueVersionVO> versionVOS = new ArrayList<>(epVersions.size());

            for (DialogueVersion v : epVersions) {
                DialogueVersionVO vvo = new DialogueVersionVO();
                vvo.setId(v.getId());
                vvo.setSourceType(v.getSourceType());
                vvo.setSourceId(v.getSourceId());
                vvo.setLanguage(v.getLanguage());
                vvo.setLanguageLabel(DialogueLanguageEnum.getLabel(v.getLanguage()));
                vvo.setFormat(v.getFormat());
                vvo.setFormatLabel(DialogueFormatEnum.getLabel(v.getFormat()));
                vvo.setScope(v.getScope());
                vvo.setScopeLabel(DialogueScopeEnum.getLabel(v.getScope()));
                vvo.setStatus(v.getStatus());

                /** 图片版本 */
                List<DialogueImage> vImages = imagesByVersion.getOrDefault(v.getId(), Collections.emptyList());
                List<DialogueImageVO> imageVOS = new ArrayList<>(vImages.size());
                for (DialogueImage img : vImages) {
                    DialogueImageVO ivo = new DialogueImageVO();
                    BeanUtils.copyProperties(img, ivo);
                    imageVOS.add(ivo);
                }
                vvo.setImages(imageVOS);

                /** 文字版本：句子 + 片段 */
                List<DialogueLine> vLines = linesByVersion.getOrDefault(v.getId(), Collections.emptyList());
                List<DialogueLineVO> lineVOS = new ArrayList<>(vLines.size());
                for (DialogueLine line : vLines) {
                    DialogueLineVO lvo = new DialogueLineVO();
                    lvo.setId(line.getId());
                    lvo.setVersionId(line.getVersionId());
                    lvo.setSpeakerId(line.getSpeakerId());
                    lvo.setSide(line.getSide());
                    lvo.setMonologue(line.getMonologue());
                    lvo.setContent(line.getContent());
                    lvo.setSort(line.getSort());

                    List<DialogueSegment> lineSegments =
                            segmentsByLine.getOrDefault(line.getId(), Collections.emptyList());
                    List<DialogueSegmentVO> segVOS = new ArrayList<>(lineSegments.size());
                    for (DialogueSegment seg : lineSegments) {
                        DialogueSegmentVO svo = new DialogueSegmentVO();
                        svo.setId(seg.getId());
                        svo.setLineId(seg.getLineId());
                        svo.setSegmentType(seg.getSegmentType());
                        svo.setContent(seg.getContent());
                        svo.setStickerId(seg.getStickerId());
                        svo.setSort(seg.getSort());

                        if (seg.getStickerId() != null) {
                            Sticker sticker = stickerMap.get(seg.getStickerId());
                            if (sticker != null) {
                                svo.setStickerLabel(sticker.getLabel());
                                svo.setStickerImageUrl(sticker.getImageUrl());
                                svo.setStickerEmoji(sticker.getEmoji());
                            }
                        }
                        segVOS.add(svo);
                    }
                    lvo.setSegments(segVOS);
                    lineVOS.add(lvo);
                }
                vvo.setLines(lineVOS);

                /** RC 选项 */
                List<RcOption> vOptions = optionsByVersion.getOrDefault(v.getId(), Collections.emptyList());
                List<RcOptionVO> optionVOS = new ArrayList<>(vOptions.size());
                for (RcOption opt : vOptions) {
                    RcOptionVO ovo = new RcOptionVO();
                    BeanUtils.copyProperties(opt, ovo);
                    optionVOS.add(ovo);
                }
                vvo.setOptions(optionVOS);

                /** 贡献者 */
                vvo.setContributors(contributorsByVersion.getOrDefault(v.getId(), Collections.emptyList()));

                versionVOS.add(vvo);
            }

            ep.setVersions(versionVOS);
        }

        return storyVO;
    }
}
