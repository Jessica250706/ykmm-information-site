package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.DialogueSourceTypeConstant;
import com.xq.constant.StatusConstant;
import com.xq.constant.SysMessageConstant;
import com.xq.constant.UserRoleConstant;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.CardDTO;
import com.xq.dto.CardImageDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.dto.CardReviewDTO;
import com.xq.entity.*;
import com.xq.enums.*;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.mapper.*;
import com.xq.result.PageResult;
import com.xq.service.CardService;
import com.xq.service.SysMessageService;
import com.xq.support.ViewerFilterSupport;
import com.xq.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 分页查询
     */
    @Override
    public PageResult<CardVO> pageQuery(CardPageQueryDTO query) {
        // ★ 关键：由角色上下文决定 status / viewerId
        // 会清空调用方传入的 status 和 viewerId，防止前端越权
        Long viewerId = ViewerFilterSupport.apply(query::setStatus);

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Card> list = cardMapper.pageQuery(query, viewerId);
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
            List<CardImage> imageList = imageMap.getOrDefault(card.getId(), Collections.emptyList());
            List<CardImageVO> imgVOs = new ArrayList<>(imageList.size());
            for (CardImage img : imageList) {
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
        Card card = cardMapper.getById(id);
        // 卡面必须存在
        if (card == null) {
            throw new RuntimeException("卡面不存在");
        }

        // 三身份可见性校验：管理员全看，游客仅已发布，用户已发布+自己创建的
        ViewerFilterSupport.checkCanView(card.getStatus(), card.getCreatorId());

        // 不再装配附属剧情对话。
        // 前端如需查看某一话的对话，调用 getEpisodeDialogue(sourceType, sourceId)。
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
        CardEpisodeVO episode = new CardEpisodeVO();
        episode.setVersions(new ArrayList<>());

        // 根据来源类型查询对应的 card_？？ 表，取到话信息
        if (sourceType == DialogueSourceTypeConstant.RC) {
            CardRcVO rc = cardRcMapper.getVOById(sourceId);
            if (rc == null) {
                throw new RuntimeException("RC 不存在");
            }
            episode.setId(rc.getId());
            episode.setCardId(rc.getCardId());
            episode.setEpisodeNo(rc.getEpisodeNo());
            episode.setTitle(rc.getTitle());
            episode.setInitiatorRoleId(rc.getRoleId());
            episode.setInitiatorRoleName(rc.getRoleName());
        } else if (sourceType == DialogueSourceTypeConstant.RTV) {
            CardRtv rtv = cardRtvMapper.getById(sourceId);
            if (rtv == null) {
                throw new RuntimeException("RTV 不存在");
            }
            episode.setId(rtv.getId());
            episode.setCardId(rtv.getCardId());
            episode.setEpisodeNo(rtv.getEpisodeNo());
            episode.setTitle(rtv.getTitle());
        } else if (sourceType == DialogueSourceTypeConstant.RABITTER) {
            CardRabitter rabitter = cardRabitterMapper.getById(sourceId);
            if (rabitter == null) {
                throw new RuntimeException("Rabitter 不存在");
            }
            episode.setId(rabitter.getId());
            episode.setCardId(rabitter.getCardId());
            episode.setEpisodeNo(rabitter.getEpisodeNo());
            episode.setTitle(rabitter.getTitle());
        }

        /* ============================================================
         * 身份过滤
         * ============================================================ */

        // 1. 先校验话所属的卡面是否对当前身份可见
        Card card = cardMapper.getById(episode.getCardId());
        if (card == null || !ViewerFilterSupport.canView(card.getStatus(), card.getCreatorId())) {
            // 统一提示"不存在"，避免通过 id 枚举探测他人待审核内容
            throw new RuntimeException("话不存在或无权查看");
        }

        // 2. 计算对话版本的过滤条件
        Integer statusFilter = null;
        Long viewerId = null;
        if (RoleContext.isGuest()) {
            statusFilter = StatusConstant.PUBLISHED;
        } else if (RoleContext.isUser()) {
            viewerId = BaseContext.getCurrentId();
        }

        Map<Long, List<DialogueVersionVO>> versionsBySource =
                loadVersionsBySource(sourceType, List.of(sourceId), statusFilter, viewerId);
        episode.setVersions(versionsBySource.getOrDefault(sourceId, Collections.emptyList()));

        return episode;
    }

    /**
     * 新增
     */
    @Override
    @Transactional
    public Long create(CardDTO dto) {
        Card card = new Card();
        BeanUtils.copyProperties(dto, card);

        switch (UserRoleEnum.current()) {
            case ADMIN -> card.setStatus(StatusConstant.PUBLISHED);
            case USER -> card.setStatus(StatusConstant.PENDING);
            case GUEST -> throw new UnauthorizedException("请先登录");
        }
        card.setCreatorId(BaseContext.getCurrentId());

        cardMapper.insert(card);
        Long cardId = card.getId();

        saveImages(cardId, dto.getImages());
        savePersonRels(cardId, dto.getPersonIds());

        log.info("新增卡面成功，id={}, status={}, creatorId={}", cardId, card.getStatus(), card.getCreatorId());
        return cardId;
    }

    /**
     * 编辑
     */
    @Override
    @Transactional
    public void update(Long id, CardDTO dto) {
        if (id == null) throw new RuntimeException("卡面ID不能为空");

        Card exist = cardMapper.getById(id);
        if (exist == null) throw new RuntimeException("卡面不存在");

        Card card = new Card();
        BeanUtils.copyProperties(dto, card);
        card.setId(id);
        switch (UserRoleEnum.current()) {
            case ADMIN -> card.setStatus(StatusConstant.PUBLISHED);
            case USER -> {
                if (!Objects.equals(exist.getCreatorId(), BaseContext.getCurrentId())) {
                    throw new ForbiddenException("只能修改自己创建的卡面");
                }
                card.setStatus(StatusConstant.PENDING);
            }
            case GUEST -> throw new UnauthorizedException("请先登录");
        }
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
        // 权限：仅管理员可删除
        // - 游客 → 未登录
        // - 用户 → 无权限（卡面下可能含他人贡献的对话版本，不能让创建者连带删除）
        if (RoleContext.isGuest()) throw new UnauthorizedException("请先登录");
        if (!RoleContext.isAdmin()) throw new ForbiddenException("只有管理员才能删除卡面，请联系管理员");

        Card exist = cardMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("卡面不存在");
        }

        // 1. 收集所有话数 id（RC / RTV / Rabitter）
        List<Long> rcIds = cardRcMapper.listIdsByCardId(id);
        List<Long> rtvIds = cardRtvMapper.listIdsByCardId(id);
        List<Long> rabitterIds = cardRabitterMapper.listIdsByCardId(id);

        // 2. 收集所有版本 id
        List<Long> versionIds = new ArrayList<>();
        if (!rcIds.isEmpty()) {
            versionIds.addAll(dialogueVersionMapper.listIdsBySource(DialogueSourceTypeConstant.RC, rcIds));
        }
        if (!rtvIds.isEmpty()) {
            versionIds.addAll(dialogueVersionMapper.listIdsBySource(DialogueSourceTypeConstant.RTV, rtvIds));
        }
        if (!rabitterIds.isEmpty()) {
            versionIds.addAll(dialogueVersionMapper.listIdsBySource(DialogueSourceTypeConstant.RABITTER, rabitterIds));
        }

        // 3. 自底向上删
        if (!versionIds.isEmpty()) {
            // 3.1 收集 lineIds
            List<Long> lineIds = dialogueLineMapper.listIdsByVersionIds(versionIds);

            if (!lineIds.isEmpty()) {
                // 删除片段
                dialogueSegmentMapper.deleteByLineIds(lineIds);
                // 删除对话行
                dialogueLineMapper.deleteByIds(lineIds);
                // RC 选项
                rcOptionMapper.deleteByVersionIds(versionIds);
            }
            // 3.2 对话图片
            dialogueImageMapper.deleteByVersionIds(versionIds);
            // 3.3 贡献者
            dialogueVersionContributorMapper.deleteByVersionIds(versionIds);
            // 3.4 版本
            dialogueVersionMapper.deleteByIds(versionIds);
        }

        // 4. 删除话数
        if (!rcIds.isEmpty()) cardRcMapper.deleteByIds(rcIds);
        if (!rtvIds.isEmpty()) cardRtvMapper.deleteByIds(rtvIds);
        if (!rabitterIds.isEmpty()) cardRabitterMapper.deleteByIds(rabitterIds);

        // 5. 删除卡面本体
        cardMapper.deleteById(id);
        cardImageMapper.deleteByCardId(id);
        cardPersonRelMapper.deleteByCardId(id);

        log.info("删除卡面成功，id={}, 操作人={}, 角色={}", id, BaseContext.getCurrentId(), UserRoleEnum.current());
    }

    @Override
    public boolean canCreate(Long cardId) {
        if (cardId == null) return false;

        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();

        // 1. 游客：不行
        if (userId == null || role == null) {
            return false;
        }

        // 2. 管理员：可以
        if (UserRoleConstant.ADMIN == role) {
            return true;
        }

        // 3. 普通用户：card.creator_id 必须等于当前用户
        Card card = cardMapper.getById(cardId);
        if (card == null) {
            return false;
        }
        return userId.equals(card.getCreatorId());
    }

    /**
     * 断言当前用户可以对指定卡面进行写操作
     */
    @Override
    public void assertWritable(Long cardId) {
        if (cardId == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        Card card = cardMapper.getById(cardId);
        if (card == null) {
            throw new RuntimeException("卡面不存在");
        }
        if (!canCreate(cardId)) {
            throw new ForbiddenException("无权操作该卡面下的内容");
        }
    }

    /**
     * 用户修改内容后，将卡面重新置为待审核；管理员操作不改变状态
     */
    @Override
    @Transactional
    public void markPendingIfNeeded(Long cardId) {
        if (cardId == null) {
            return;
        }
        // 管理员：不改变状态
        if (RoleContext.isAdmin()) {
            return;
        }
        // 游客：理论上已被 assertWritable 拦截，这里兜底
        if (RoleContext.isGuest()) {
            return;
        }
        // 普通用户：置为待审核，并清空上一次的审核记录
        cardMapper.markPending(cardId);
        log.info("用户修改卡面下的内容，卡面回到待审核状态，cardId={}, userId={}", cardId, BaseContext.getCurrentId());
    }

    /**
     * 审核卡面
     * <p>
     * 规则：
     * <ul>
     *   <li>仅管理员可操作</li>
     *   <li>仅 PENDING（待审核）状态可审核</li>
     *   <li>审核结果只允许「通过 1 / 拒绝 3」</li>
     *   <li>拒绝时必须填写审核备注</li>
     * </ul>
     */
    @Override
    @Transactional
    public void review(Long id, CardReviewDTO dto) {
        // 1. 身份校验
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能审核卡面");
        }

        // 2. 参数校验
        if (dto == null || dto.getStatus() == null) {
            throw new RuntimeException("审核状态不能为空");
        }
        Integer targetStatus = dto.getStatus();
        boolean pass = StatusEnum.PUBLISHED.getValue().equals(targetStatus);
        boolean reject = StatusEnum.REJECTED.getValue().equals(targetStatus);
        if (!pass && !reject) {
            throw new RuntimeException("审核状态只能为「已发布」或「已拒绝」");
        }
        String remark = dto.getReviewRemark();
        if (reject && (remark == null || remark.isBlank())) {
            throw new RuntimeException("拒绝时必须填写审核备注");
        }

        // 3. 存在性 + 状态检查
        Card exist = cardMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("卡面不存在");
        }
        if (!StatusEnum.PENDING.getValue().equals(exist.getStatus())) {
            throw new RuntimeException("当前卡面不是待审核状态，无法审核");
        }

        // 4. 更新审核字段
        Card update = new Card();
        update.setId(id);
        update.setStatus(targetStatus);
        update.setReviewerId(BaseContext.getCurrentId());
        update.setReviewTime(LocalDateTime.now());
        // 通过时若未填备注，显式写空串，避免把上一次「已拒绝」遗留的备注带出来
        update.setReviewRemark(remark == null ? "" : remark.trim());

        cardMapper.update(update);

        if (pass) {
            sysMessageService.send(
                    exist.getCreatorId(),
                    SysMessageConstant.TYPE_REVIEW_PASSED,
                    SysMessageConstant.TARGET_CARD,
                    id,
                    "卡面审核通过",
                    "您提交的卡面「" + exist.getName() + "」已通过审核");
        } else {
            sysMessageService.send(
                    exist.getCreatorId(),
                    SysMessageConstant.TYPE_REVIEW_REJECTED,
                    SysMessageConstant.TARGET_CARD,
                    id,
                    "卡面审核未通过",
                    "您提交的卡面「" + exist.getName() + "」未通过审核，原因：" + remark);
        }

        log.info("审核卡面成功，id={}, status={}, reviewerId={}, remark={}",
                id, targetStatus, BaseContext.getCurrentId(), update.getReviewRemark());
    }

    /**
     * 分页查询当前用户创建的卡面
     */
    @Override
    public PageResult<CardVO> pageMyCards(CardPageQueryDTO query) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Card> list = cardMapper.pageQueryByCreator(query, userId);
        PageInfo<Card> page = new PageInfo<>(list);

        List<CardVO> voList = assembleCardVOs(page.getList());
        return new PageResult<>(page.getTotal(), voList);
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
            List<CardImage> imageList = cardImageMapper.listByCardId(card.getId());
            List<CardImageVO> imgVOs = new ArrayList<>(imageList.size());
            for (CardImage img : imageList) {
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
     * 把 Card 实体列表组装成完整 VO（含图片、人物），供多个分页方法复用
     */
    private List<CardVO> assembleCardVOs(List<Card> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> cardIds = list.stream()
                .map(Card::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, List<CardImage>> imageMap = cardIds.isEmpty()
                ? Collections.emptyMap()
                : cardImageMapper.listByCardIds(cardIds).stream()
                .collect(Collectors.groupingBy(CardImage::getCardId));

        Map<Long, List<CardPersonVO>> personMap = cardIds.isEmpty()
                ? Collections.emptyMap()
                : cardPersonRelMapper.listPersonsByCardIds(cardIds).stream()
                .collect(Collectors.groupingBy(CardPersonVO::getCardId));

        List<CardVO> voList = new ArrayList<>(list.size());
        for (Card card : list) {
            CardVO vo = toVO(card, false);

            List<CardImage> imageList = imageMap.getOrDefault(card.getId(), Collections.emptyList());
            List<CardImageVO> imgVOs = new ArrayList<>(imageList.size());
            for (CardImage img : imageList) {
                CardImageVO ivo = new CardImageVO();
                BeanUtils.copyProperties(img, ivo);
                ivo.setImageTypeLabel(CardImageTypeEnum.getLabel(img.getImageType()));
                imgVOs.add(ivo);
            }
            vo.setImages(imgVOs);
            vo.setPersons(new ArrayList<>(personMap.getOrDefault(card.getId(), Collections.emptyList())));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 根据来源类型 + 来源ID列表，批量加载对话版本及其内容
     *
     * @param sourceType   来源类型
     * @param sourceIds    来源ID列表
     * @param statusFilter 审核状态过滤，null 表示不过滤
     * @param viewerId     访客ID：null 不按创建者过滤；非 null 则查"已发布 OR 自己创建的"
     * @return sourceId -> 版本VO列表
     */
    private Map<Long, List<DialogueVersionVO>> loadVersionsBySource(
            Integer sourceType,
            List<Long> sourceIds,
            Integer statusFilter,
            Long viewerId) {

        if (sourceIds == null || sourceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量查询对话版本
        List<DialogueVersion> versions =
                dialogueVersionMapper.listBySourceIds(sourceType, sourceIds, statusFilter, viewerId);

        if (versions.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> versionIds = versions.stream()
                .map(DialogueVersion::getId)
                .toList();

        // 批量查询对话句子
        List<DialogueLine> lines = dialogueLineMapper.listByVersionIds(versionIds);
        Map<Long, List<DialogueLine>> linesByVersion = lines.stream()
                .collect(Collectors.groupingBy(DialogueLine::getVersionId));

        // 说话人：基于所有 speakerId 批量查 role，再查 person
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

        // 批量查询片段
        List<Long> lineIds = lines.stream().map(DialogueLine::getId).toList();
        List<DialogueSegment> segments = lineIds.isEmpty()
                ? Collections.emptyList()
                : dialogueSegmentMapper.listByLineIds(lineIds);
        Map<Long, List<DialogueSegment>> segmentsByLine = segments.stream()
                .collect(Collectors.groupingBy(DialogueSegment::getLineId));

        // 批量查询表情包
        Set<Long> stickerIds = segments.stream()
                .map(DialogueSegment::getStickerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Sticker> stickerMap = stickerIds.isEmpty()
                ? Collections.emptyMap()
                : stickerMapper.listByIds(new ArrayList<>(stickerIds)).stream()
                .collect(Collectors.toMap(Sticker::getId, s -> s, (a, b) -> a));

        // 批量查询图片版本
        List<DialogueImage> images = dialogueImageMapper.listByVersionIds(versionIds);
        Map<Long, List<DialogueImage>> imagesByVersion = images.stream()
                .collect(Collectors.groupingBy(DialogueImage::getVersionId));

        // 批量查询 RC 选项（仅 RC），同时构建 lineId -> 角色 / 选项编号 映射
        Map<Long, List<RcOption>> optionsByVersion;
        Map<Long, Integer> lineRoleMap = new HashMap<>();
        Map<Long, Integer> lineOptionNumMap = new HashMap<>();

        if (DialogueSourceTypeEnum.RC.getValue().equals(sourceType)) {
            List<RcOption> options = rcOptionMapper.listByVersionIds(versionIds);
            optionsByVersion = options.stream()
                    .collect(Collectors.groupingBy(RcOption::getVersionId));

            // 用 rc_option 反查每一行的角色和选项编号
            for (RcOption opt : options) {
                Integer optNum = opt.getSort();
                if (opt.getQuestionLineId() != null) {
                    lineRoleMap.put(opt.getQuestionLineId(), DialogueRoleEnum.QUESTION.getValue());
                    lineOptionNumMap.put(opt.getQuestionLineId(), optNum);
                }
                if (opt.getAnswerLineId() != null) {
                    lineRoleMap.put(opt.getAnswerLineId(), DialogueRoleEnum.ANSWER.getValue());
                    lineOptionNumMap.put(opt.getAnswerLineId(), optNum);
                }
            }
        } else {
            optionsByVersion = Collections.emptyMap();
        }

        // 批量查询贡献者
        List<DialogueVersionContributorVO> contributors =
                dialogueVersionContributorMapper.listByVersionIds(versionIds);
        Map<Long, List<DialogueVersionContributorVO>> contributorsByVersion = contributors.stream()
                .collect(Collectors.groupingBy(DialogueVersionContributorVO::getVersionId));

        // 构建版本VO列表
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

            // 图片版本
            List<DialogueImage> vImages = imagesByVersion.getOrDefault(v.getId(), Collections.emptyList());
            List<DialogueImageVO> imageVOS = new ArrayList<>(vImages.size());
            for (DialogueImage img : vImages) {
                DialogueImageVO ivo = new DialogueImageVO();
                BeanUtils.copyProperties(img, ivo);
                imageVOS.add(ivo);
            }
            vvo.setImages(imageVOS);

            // 文字版本：句子 + 片段
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

                // 从 rc_option 反查角色和选项编号
                Integer roleValue = lineRoleMap.getOrDefault(
                        line.getId(), DialogueRoleEnum.NORMAL.getValue());
                lvo.setDialogueRole(roleValue);
                lvo.setDialogueRoleLabel(DialogueRoleEnum.getLabel(roleValue));
                lvo.setOptionNumber(lineOptionNumMap.get(line.getId()));

                // 通过 role 反查说话人及人物信息
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

            // RC 选项
            List<RcOption> vOptions = optionsByVersion.getOrDefault(v.getId(), Collections.emptyList());
            List<RcOptionVO> optionVOS = new ArrayList<>(vOptions.size());
            for (RcOption opt : vOptions) {
                RcOptionVO ovo = new RcOptionVO();
                BeanUtils.copyProperties(opt, ovo);
                optionVOS.add(ovo);
            }
            vvo.setOptions(optionVOS);

            // 贡献者
            vvo.setContributors(contributorsByVersion.getOrDefault(v.getId(), Collections.emptyList()));

            versionVOS.add(vvo);
        }

        // 按 source_id 分组返回
        return versionVOS.stream()
                .collect(Collectors.groupingBy(DialogueVersionVO::getSourceId));
    }
}
