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
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PersonMapper personMapper;

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
        /* 卡面ID不能为空 */
        if (id == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        Card card = cardMapper.getById(id);
        /* 卡面必须存在 */
        if (card == null) {
            throw new RuntimeException("卡面不存在");
        }

        if (RoleContext.isUser()) {
            /* 用户端只允许查看已发布卡面 */
            if (!StatusEnum.PUBLISHED.getValue().equals(card.getStatus())) {
                throw new RuntimeException("卡面不存在");
            }
        }

        /*
          不再装配附属剧情对话。
          前端如需查看某一话的对话，调用 getEpisodeDialogue(sourceType, sourceId)。
         */
        return toVO(card, true);
    }

    /**
     * 查询某一话的对话
     *
     * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter
     * @param sourceId   来源ID
     * @return 话详情（含对话版本）
     */
    @Override
    public CardEpisodeVO getEpisodeDialogue(Integer sourceType, Long sourceId) {
        /** 来源类型不能为空 */
        if (sourceType == null) {
            throw new RuntimeException("来源类型不能为空");
        }
        /** 来源ID不能为空 */
        if (sourceId == null) {
            throw new RuntimeException("来源ID不能为空");
        }
        /** 只支持 RC / RTV / Rabitter */
        if (!DialogueSourceTypeEnum.RC.getValue().equals(sourceType)
                && !DialogueSourceTypeEnum.RTV.getValue().equals(sourceType)
                && !DialogueSourceTypeEnum.RABITTER.getValue().equals(sourceType)) {
            throw new RuntimeException("该来源类型不支持查询卡面附属剧情");
        }

        CardEpisodeVO episode = new CardEpisodeVO();
        episode.setVersions(new ArrayList<>());

        /** 根据来源类型查询对应的 card_？？ 表，取到话信息 */
        if (DialogueSourceTypeEnum.RC.getValue().equals(sourceType)) {
            // RC → card_rc
            CardRcVO rc = cardRcMapper.getVOById(sourceId);
            /** RC 必须存在 */
            if (rc == null) {
                throw new RuntimeException("RC 不存在");
            }
            episode.setId(rc.getId());
            episode.setCardId(rc.getCardId());
            episode.setEpisodeNo(rc.getEpisodeNo());
            episode.setTitle(rc.getTitle());
            episode.setInitiatorRoleId(rc.getRoleId());
            episode.setInitiatorRoleName(rc.getRoleName());
        } else if (DialogueSourceTypeEnum.RTV.getValue().equals(sourceType)) {
            // RTV → card_rtv
            CardRtv rtv = cardRtvMapper.getById(sourceId);
            /** RTV 必须存在 */
            if (rtv == null) {
                throw new RuntimeException("RTV 不存在");
            }
            episode.setId(rtv.getId());
            episode.setCardId(rtv.getCardId());
            episode.setEpisodeNo(rtv.getEpisodeNo());
            episode.setTitle(rtv.getTitle());
        } else {
            // Rabitter → card_rabitter
            CardRabitter rabitter = cardRabitterMapper.getById(sourceId);
            /** Rabitter 必须存在 */
            if (rabitter == null) {
                throw new RuntimeException("Rabitter 不存在");
            }
            episode.setId(rabitter.getId());
            episode.setCardId(rabitter.getCardId());
            episode.setEpisodeNo(rabitter.getEpisodeNo());
            episode.setTitle(rabitter.getTitle());
        }

        /** 用户端只允许查看已发布卡面下的对话 */
        if (RoleContext.isUser()) {
            Card card = cardMapper.getById(episode.getCardId());
            if (card == null || !StatusEnum.PUBLISHED.getValue().equals(card.getStatus())) {
                throw new RuntimeException("卡面不存在");
            }
        }

        Integer statusFilter = RoleContext.isUser() ? StatusEnum.PUBLISHED.getValue() : null;
        Map<Long, List<DialogueVersionVO>> versionsBySource =
                loadVersionsBySource(sourceType, List.of(sourceId), statusFilter);
        episode.setVersions(versionsBySource.getOrDefault(sourceId, Collections.emptyList()));

        return episode;
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
     * 根据来源类型 + 来源ID列表，批量加载对话版本及其内容
     *
     * @param sourceType   来源类型
     * @param sourceIds    来源ID列表
     * @param statusFilter 审核状态过滤，null 表示不过滤
     * @return sourceId -> 版本VO列表
     */
    private Map<Long, List<DialogueVersionVO>> loadVersionsBySource(
            Integer sourceType, List<Long> sourceIds, Integer statusFilter) {

        if (sourceIds == null || sourceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        /** 批量查询对话版本 */
        List<DialogueVersion> versions =
                dialogueVersionMapper.listBySourceIds(sourceType, sourceIds, statusFilter);
        if (versions.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> versionIds = versions.stream()
                .map(DialogueVersion::getId)
                .toList();

        /** 批量查询对话句子 */
        List<DialogueLine> lines = dialogueLineMapper.listByVersionIds(versionIds);
        Map<Long, List<DialogueLine>> linesByVersion = lines.stream()
                .collect(Collectors.groupingBy(DialogueLine::getVersionId));

        /** 说话人：基于所有 speakerId 批量查 role，再查 person */
        Set<Long> speakerIds = lines.stream()
                .map(DialogueLine::getSpeakerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Role> roleMap = speakerIds.isEmpty()
                ? Collections.emptyMap()
                : roleMapper.listByIds(speakerIds).stream()
                .collect(Collectors.toMap(Role::getId, r -> r, (a, b) -> a));

        Set<Long> personIds = roleMap.values().stream()
                .map(Role::getPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Person> personMap = personIds.isEmpty()
                ? Collections.emptyMap()
                : personMapper.listByIds(personIds).stream()
                .collect(Collectors.toMap(Person::getId, p -> p, (a, b) -> a));

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
        Map<Long, Sticker> stickerMap = stickerIds.isEmpty()
                ? Collections.emptyMap()
                : stickerMapper.listByIds(new ArrayList<>(stickerIds)).stream()
                .collect(Collectors.toMap(Sticker::getId, s -> s, (a, b) -> a));

        /** 批量查询图片版本 */
        List<DialogueImage> images = dialogueImageMapper.listByVersionIds(versionIds);
        Map<Long, List<DialogueImage>> imagesByVersion = images.stream()
                .collect(Collectors.groupingBy(DialogueImage::getVersionId));

        /** 批量查询 RC 选项（仅 RC） */
        Map<Long, List<RcOption>> optionsByVersion;
        if (DialogueSourceTypeEnum.RC.getValue().equals(sourceType)) {
            List<RcOption> options = rcOptionMapper.listByVersionIds(versionIds);
            optionsByVersion = options.stream()
                    .collect(Collectors.groupingBy(RcOption::getVersionId));
        } else {
            optionsByVersion = Collections.emptyMap();
        }

        /** 批量查询贡献者 */
        List<DialogueVersionContributorVO> contributors =
                dialogueVersionContributorMapper.listByVersionIds(versionIds);
        Map<Long, List<DialogueVersionContributorVO>> contributorsByVersion = contributors.stream()
                .collect(Collectors.groupingBy(DialogueVersionContributorVO::getVersionId));

        /** 构建版本VO列表 */
        List<DialogueVersionVO> versionVOS = new ArrayList<>(versions.size());
        for (DialogueVersion v : versions) {
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

                /** 通过 role 反查说话人及人物信息 */
                Role role = roleMap.get(line.getSpeakerId());
                if (role != null) {
                    lvo.setSpeakerName(role.getName());        // 角色名 → speakerName
                    lvo.setPersonId(role.getPersonId());       // 角色关联的人物ID → personId

                    Person person = personMap.get(role.getPersonId());
                    if (person != null) {
                        lvo.setPersonNameCn(person.getNameCn());          // 人物中文名
                        lvo.setPersonAvatar(person.getAvatar());          // 人物头像
                        lvo.setPersonThemeColor(person.getThemeColor());  // 人物代表色
                    }
                }

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

        /** 按 source_id 分组返回 */
        return versionVOS.stream()
                .collect(Collectors.groupingBy(DialogueVersionVO::getSourceId));
    }
}
