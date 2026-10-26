package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.SysMessageConstant;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.DialogueVersionDTO;
import com.xq.dto.DialogueVersionPageQueryDTO;
import com.xq.dto.DialogueVersionReviewDTO;
import com.xq.entity.*;
import com.xq.enums.*;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.mapper.*;
import com.xq.result.PageResult;
import com.xq.service.DialogueVersionContributorService;
import com.xq.service.DialogueVersionService;
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
 * 对话版本服务实现
 */
@Service
@Slf4j
public class DialogueVersionServiceImpl implements DialogueVersionService {

    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;
    @Autowired
    private DialogueLineMapper dialogueLineMapper;
    @Autowired
    private DialogueSegmentMapper dialogueSegmentMapper;
    @Autowired
    private DialogueImageMapper dialogueImageMapper;
    @Autowired
    private DialogueVersionContributorService dialogueVersionContributorService;
    @Autowired
    private DialogueVersionContributorMapper dialogueVersionContributorMapper;
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private CardSeriesMapper cardSeriesMapper;
    @Autowired
    private CardRcMapper cardRcMapper;
    @Autowired
    private CardRtvMapper cardRtvMapper;
    @Autowired
    private CardRabitterMapper cardRabitterMapper;
    @Autowired
    private StoryMapper storyMapper;
    @Autowired
    private StoryCategoryMapper storyCategoryMapper;
    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 分页查询（三身份过滤）
     */
    @Override
    public PageResult<DialogueVersionVO> pageQuery(DialogueVersionPageQueryDTO query) {
        // 由角色上下文决定 status / viewerId
        Long viewerId = ViewerFilterSupport.apply(query::setStatus);

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<DialogueVersion> list = dialogueVersionMapper.pageQuery(query, viewerId);
        PageInfo<DialogueVersion> page = new PageInfo<>(list);

        List<DialogueVersionVO> voList = page.getList().stream()
                .map(this::toBasicVO)
                .collect(Collectors.toList());

        fillSourceInfo(voList);

        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 管理端：按来源查询全部版本
     */
    @Override
    public List<DialogueVersionVO> listBySource(Integer sourceType, Long sourceId) {
        List<DialogueVersion> list = dialogueVersionMapper.listBySource(sourceType, sourceId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toBasicVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询版本详情（三身份可见性校验）
     */
    @Override
    public DialogueVersionVO detail(Long versionId) {
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }

        // 三身份可见性校验
        ViewerFilterSupport.checkCanView(version.getStatus(), version.getCreatorId());

        DialogueVersionVO vo = toBasicVO(version);

        // 按 format 加载内容
        if (DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            vo.setLines(loadLines(versionId));
        } else if (DialogueFormatEnum.IMAGE.getValue().equals(version.getFormat())) {
            vo.setImages(loadImages(versionId));
        }
        return vo;
    }

    /**
     * 创建版本
     * <p>
     * - 管理员：直接已发布，reviewer = 自己
     * - 用户：待审核
     * - 游客：拒绝
     */
    @Override
    @Transactional
    public Long create(DialogueVersionDTO dto) {
        // 唯一性校验（业务）
        DialogueVersion exist = dialogueVersionMapper.getByUniqueKey(
                dto.getSourceType(), dto.getSourceId(),
                dto.getLanguage(), dto.getFormat(), dto.getScope());
        if (exist != null) {
            throw new RuntimeException("该组合的对话版本已存在");
        }

        DialogueVersion version = new DialogueVersion();
        BeanUtils.copyProperties(dto, version);

        Long currentUserId = BaseContext.getCurrentId();
        Integer creatorRole = BaseContext.getCurrentRole();
        version.setCreatorId(currentUserId);
        version.setCreatorRole(creatorRole);

        // 身份决定初始状态
        switch (UserRoleEnum.current()) {
            case ADMIN -> {
                version.setStatus(StatusEnum.PUBLISHED.getValue());
                version.setReviewerId(currentUserId);
                version.setReviewTime(LocalDateTime.now());
                version.setReviewRemark("管理员直接发布");
            }
            case USER -> {
                version.setStatus(StatusEnum.PENDING.getValue());
                version.setReviewerId(null);
                version.setReviewTime(null);
                version.setReviewRemark(null);
            }
            case GUEST -> throw new UnauthorizedException("请先登录");
        }

        dialogueVersionMapper.insert(version);

        Long versionId = version.getId();
        log.info("创建对话版本成功，id={}, status={}, creatorId={}",
                versionId, version.getStatus(), currentUserId);

        // 记录贡献者
        dialogueVersionContributorService.recordCreator(
                versionId,
                currentUserId,
                creatorRole,
                dto.getContributorUserId(),
                dto.getContributorName());

        // 用户提交对话版本 → 广播给管理员
        if (UserRoleEnum.current() == UserRoleEnum.USER) {
            // 来源描述用于消息正文，尽量给管理员一个直观的"这是什么"
            String sourceHint = buildSourceHint(dto.getSourceType(), dto.getSourceId());
            sysMessageService.sendToRole(
                    SysMessageConstant.BROADCAST_ROLE_ADMIN,
                    SysMessageConstant.TYPE_SYSTEM,
                    SysMessageConstant.TARGET_DIALOGUE_VERSION,
                    versionId,
                    "新对话版本待审核",
                    "用户提交了新的对话版本（" + sourceHint + "），请及时审核");
        }

        return versionId;
    }

    /**
     * 删除版本
     * <p>
     * - 管理员：可删任意
     * - 用户：只能删自己创建的
     * - 游客：拒绝
     */
    @Override
    @Transactional
    public void delete(Long versionId) {
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }

        // 权限：三身份
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (RoleContext.isUser()) {
            if (!Objects.equals(version.getCreatorId(), BaseContext.getCurrentId())) {
                throw new ForbiddenException("只能删除自己创建的对话版本");
            }
        }
        // 管理员直接放行

        // 级联删除文字内容
        List<DialogueLine> lines = dialogueLineMapper.listByVersionId(versionId);
        if (lines != null && !lines.isEmpty()) {
            List<Long> lineIds = lines.stream()
                    .map(DialogueLine::getId)
                    .collect(Collectors.toList());
            dialogueSegmentMapper.deleteByLineIds(lineIds);
            dialogueLineMapper.deleteByVersionId(versionId);
        }

        // 级联删除图片内容
        dialogueImageMapper.deleteByVersionId(versionId);

        // 删除版本
        dialogueVersionMapper.deleteById(versionId);

        // 级联清理贡献者
        dialogueVersionContributorService.deleteByVersionId(versionId);

        log.info("删除对话版本成功，id={}, 操作人={}, 角色={}",
                versionId, BaseContext.getCurrentId(), UserRoleEnum.current());
    }

    /* ============================================================
     * 版本选项（三身份过滤）
     * ============================================================ */

    @Override
    public List<DialogueVersionOptionVO> listTextVersionOptions(Integer sourceType, Long sourceId) {
        return buildVersionOptions(sourceType, sourceId, EnumSet.of(DialogueFormatEnum.TEXT));
    }

    @Override
    public List<DialogueVersionOptionVO> listImageVersionOptions(Integer sourceType, Long sourceId) {
        return buildVersionOptions(sourceType, sourceId, EnumSet.of(DialogueFormatEnum.IMAGE));
    }

    @Override
    public List<DialogueVersionOptionVO> listVersionOptions(Integer sourceType, Long sourceId) {
        return buildVersionOptions(sourceType, sourceId, EnumSet.allOf(DialogueFormatEnum.class));
    }

    /**
     * 构建版本选项列表
     * <p>
     * 返回的每个 option 会带上 canOperate 字段：
     * - 管理员：已存在的版本一律 true
     * - 游客：一律 false
     * - 用户：creator_id 是自己，或在 contributor 表中角色为「内容作者 / 协作者」
     * 版本不存在时 canOperate 恒为 false（代表"没有可修改的内容"）
     */
    private List<DialogueVersionOptionVO> buildVersionOptions(
            Integer sourceType, Long sourceId, Set<DialogueFormatEnum> formats) {

        if (!DialogueSourceTypeEnum.isValid(sourceType)) {
            throw new RuntimeException("来源类型不合法");
        }
        if (sourceId == null) {
            throw new RuntimeException("来源ID不能为空");
        }

        // 计算 viewer 过滤条件
        Integer statusFilter = null;
        Long viewerId = null;
        if (RoleContext.isGuest()) {
            statusFilter = StatusEnum.PUBLISHED.getValue();
        } else if (RoleContext.isUser()) {
            viewerId = BaseContext.getCurrentId();
        }

        // 查库
        List<DialogueVersion> existed = dialogueVersionMapper.listBySourceIds(
                sourceType, List.of(sourceId), statusFilter, viewerId);

        // 建索引：language + format + scope -> 版本实体
        Map<String, DialogueVersion> existedMap = new HashMap<>();
        if (existed != null) {
            for (DialogueVersion v : existed) {
                existedMap.put(keyOf(v.getLanguage(), v.getFormat(), v.getScope()), v);
            }
        }

        // 预计算：当前用户可编辑的版本ID集合
        Set<Long> editableVersionIds = resolveEditableVersionIds(existed);

        // 批量查询所有已存在版本的贡献者（一次查询，避免循环内 N+1）
        Map<Long, List<DialogueVersionContributorVO>> contributorsMap = Collections.emptyMap();
        if (existed != null && !existed.isEmpty()) {
            List<Long> versionIds = existed.stream()
                    .map(DialogueVersion::getId)
                    .filter(Objects::nonNull)
                    .toList();
            if (!versionIds.isEmpty()) {
                List<DialogueVersionContributorVO> allContributors =
                        dialogueVersionContributorMapper.listByVersionIds(versionIds);
                if (allContributors != null && !allContributors.isEmpty()) {
                    contributorsMap = allContributors.stream()
                            .collect(Collectors.groupingBy(DialogueVersionContributorVO::getVersionId));
                }
            }
        }

        // 是否游客：游客只返回已存在的版本，不展示"可创建"的空选项
        boolean guest = RoleContext.isGuest();

        List<DialogueVersionOptionVO> result = new ArrayList<>();
        for (DialogueLanguageEnum lang : DialogueLanguageEnum.values()) {
            for (DialogueScopeEnum scope : DialogueScopeEnum.values()) {
                for (DialogueFormatEnum format : DialogueFormatEnum.values()) {
                    if (!formats.contains(format)) continue;

                    DialogueVersion matched = existedMap.get(
                            keyOf(lang.getValue(), format.getValue(), scope.getValue()));
                    Long versionId = matched != null ? matched.getId() : null;

                    // 游客：跳过不存在的组合
                    if (guest && matched == null) continue;

                    DialogueVersionOptionVO vo = new DialogueVersionOptionVO();
                    vo.setLanguage(lang.getValue());
                    vo.setLanguageLabel(lang.getLabel());
                    vo.setFormat(format.getValue());
                    vo.setFormatLabel(format.getLabel());
                    vo.setScope(scope.getValue());
                    vo.setScopeLabel(scope.getLabel());
                    vo.setLabel(lang.getLabel() + " · " + format.getLabel()
                            + " · " + scope.getLabel());

                    vo.setVersionId(versionId);
                    vo.setExists(matched != null);
                    // 版本不存在时无内容可改，恒为 false
                    vo.setCanOperate(matched != null && editableVersionIds.contains(versionId));

                    // 状态 + 贡献者
                    if (matched != null) {
                        vo.setStatus(matched.getStatus());
                        vo.setStatusLabel(StatusEnum.getLabel(matched.getStatus()));
                        vo.setContributors(contributorsMap.getOrDefault(versionId, Collections.emptyList()));
                    } else {
                        // 不存在的组合：状态为 null，贡献者为空列表
                        vo.setContributors(Collections.emptyList());
                    }

                    result.add(vo);
                }
            }
        }
        return result;
    }

    /**
     * 分页查询当前用户创建的版本（含来源展示信息）
     */
    @Override
    public PageResult<DialogueVersionVO> pageMyVersions(DialogueVersionPageQueryDTO query) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<DialogueVersion> list = dialogueVersionMapper.pageQueryByCreator(query, userId);
        PageInfo<DialogueVersion> page = new PageInfo<>(list);

        List<DialogueVersionVO> voList = page.getList().stream()
                .map(this::toBasicVO)
                .collect(Collectors.toList());

        // 填充来源展示信息
        fillSourceInfo(voList);

        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 审核对话版本
     */
    @Override
    @Transactional
    public void review(Long id, DialogueVersionReviewDTO dto) {
        // 1. 身份校验：仅管理员
        if (RoleContext.isGuest()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!RoleContext.isAdmin()) {
            throw new ForbiddenException("只有管理员才能审核对话版本");
        }

        // 2. 参数校验
        if (id == null) {
            throw new RuntimeException("版本ID不能为空");
        }
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
        DialogueVersion exist = dialogueVersionMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("对话版本不存在");
        }
        if (!StatusEnum.PENDING.getValue().equals(exist.getStatus())) {
            throw new RuntimeException("当前对话版本不是待审核状态，无法审核");
        }

        // 4. 更新审核字段
        Long currentUserId = BaseContext.getCurrentId();
        DialogueVersion update = new DialogueVersion();
        update.setId(id);
        update.setStatus(targetStatus);
        update.setReviewerId(currentUserId);
        update.setReviewTime(LocalDateTime.now());
        // 通过时若未填备注，显式写空串，避免把上一次「已拒绝」遗留的备注带出来
        update.setReviewRemark(remark == null ? "" : remark.trim());

        dialogueVersionMapper.review(update);

        // 5. 向创建者推送审核结果消息
        //    管理员审核自己创建的内容属于异常场景（正常流程下管理员创建即发布，不会走审核），
        //    但这里仍做防御性判断，避免给自己发消息。
        Long creatorId = exist.getCreatorId();
        if (creatorId != null && !Objects.equals(creatorId, currentUserId)) {
            // 拼上来源提示，让用户能看出是哪一份内容被审核
            String sourceHint = buildSourceHint(exist.getSourceType(), exist.getSourceId());

            if (pass) {
                sysMessageService.send(
                        creatorId,
                        SysMessageConstant.TYPE_REVIEW_PASSED,
                        SysMessageConstant.TARGET_DIALOGUE_VERSION,
                        id,
                        "对话版本审核通过",
                        "您提交的对话版本（" + sourceHint + "）已通过审核");
            } else {
                sysMessageService.send(
                        creatorId,
                        SysMessageConstant.TYPE_REVIEW_REJECTED,
                        SysMessageConstant.TARGET_DIALOGUE_VERSION,
                        id,
                        "对话版本审核未通过",
                        "您提交的对话版本（" + sourceHint + "）未通过审核，原因：" + remark);
            }
        }

        log.info("审核对话版本成功，id={}, status={}, reviewerId={}, remark={}",
                id, targetStatus, currentUserId, update.getReviewRemark());
    }

    /* ============================================================
     * 私有方法
     * ============================================================ */

    /**
     * 为版本列表批量填充来源展示信息。
     * <p>
     * 说明：一页最多 pageSize（默认 20）条，每条最多 3 次主键查询，
     * 总查询次数可控（毫秒级）；如果后续 pageSize 调大或对性能有更高要求，
     * 可改写为按类型 listByIds 批量查询。
     */
    private void fillSourceInfo(List<DialogueVersionVO> voList) {
        if (voList == null || voList.isEmpty()) return;
        for (DialogueVersionVO vo : voList) {
            vo.setSource(buildSourceInfo(vo.getSourceType(), vo.getSourceId()));
        }
    }

    /**
     * 构建单个版本的来源展示信息
     */
    private DialogueVersionSourceVO buildSourceInfo(Integer sourceType, Long sourceId) {
        if (sourceType == null || sourceId == null) return null;

        DialogueVersionSourceVO src = new DialogueVersionSourceVO();
        src.setSourceType(sourceType);
        src.setSourceId(sourceId);

        // ---------- 剧情：story.id ----------
        if (DialogueSourceTypeEnum.STORY.getValue().equals(sourceType)) {
            Story story = storyMapper.getById(sourceId);
            if (story == null) return src;
            src.setTitle(story.getTitle());
            if (story.getCategoryId() != null) {
                StoryCategory cat = storyCategoryMapper.getById(story.getCategoryId());
                if (cat != null) {
                    src.setSubtitle(cat.getName());
                    src.setCategoryType(cat.getCategoryType());
                }
            }
            return src;
        }

        // ---------- 卡面类：card_rc.id / card_rtv.id / card_rabitter.id ----------
        Long cardId = null;
        Integer episodeNo = null;
        String episodeTitle = null;

        if (DialogueSourceTypeEnum.RC.getValue().equals(sourceType)) {
            CardRc rc = cardRcMapper.getById(sourceId);
            if (rc != null) {
                cardId = rc.getCardId();
                episodeNo = rc.getEpisodeNo();
                episodeTitle = rc.getTitle();
            }
        } else if (DialogueSourceTypeEnum.RTV.getValue().equals(sourceType)) {
            CardRtv rtv = cardRtvMapper.getById(sourceId);
            if (rtv != null) {
                cardId = rtv.getCardId();
                episodeNo = rtv.getEpisodeNo();
                episodeTitle = rtv.getTitle();
            }
        } else if (DialogueSourceTypeEnum.RABITTER.getValue().equals(sourceType)) {
            CardRabitter rabitter = cardRabitterMapper.getById(sourceId);
            if (rabitter != null) {
                cardId = rabitter.getCardId();
                episodeNo = rabitter.getEpisodeNo();
                episodeTitle = rabitter.getTitle();
            }
        }

        src.setCardId(cardId);
        src.setEpisodeNo(episodeNo);
        src.setEpisodeTitle(episodeTitle);

        // 卡面名称 + 系列名
        if (cardId != null) {
            Card card = cardMapper.getById(cardId);
            if (card != null) {
                src.setTitle(card.getName());
                if (card.getSeriesId() != null) {
                    CardSeries series = cardSeriesMapper.getById(card.getSeriesId());
                    if (series != null) src.setSubtitle(series.getName());
                }
            }
        }
        return src;
    }

    /**
     * 计算当前用户对「已存在版本」集合中每个版本的编辑权限。
     * <p>
     * 规则：
     * <ul>
     *   <li>游客：空集合（一律不可编辑）</li>
     *   <li>管理员：所有已存在版本都可编辑</li>
     *   <li>普通用户：满足以下任一条件即可编辑
     *     <ol>
     *       <li>creator_id = 自己（自己上传的）</li>
     *       <li>在 dialogue_version_contributor 中，且 contributor_role ∈ {1 内容作者, 3 协作者}</li>
     *     </ol>
     *     —— 管理员"代传"（role=2）不授予用户编辑权，与业务约定一致。
     *   </li>
     * </ul>
     *
     * @param existed 已存在的版本列表（可为 null）
     * @return 当前用户可编辑的版本ID集合（不会为 null）
     */
    private Set<Long> resolveEditableVersionIds(List<DialogueVersion> existed) {
        if (existed == null || existed.isEmpty()) {
            return Collections.emptySet();
        }

        // 游客：一律不可编辑
        if (RoleContext.isGuest()) {
            return Collections.emptySet();
        }

        // 管理员：全部版本均可编辑
        if (RoleContext.isAdmin()) {
            return existed.stream()
                    .map(DialogueVersion::getId)
                    .collect(Collectors.toSet());
        }

        // 普通用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Collections.emptySet();
        }

        Set<Long> result = new HashSet<>();

        // 1. 自己创建的版本
        for (DialogueVersion v : existed) {
            if (Objects.equals(v.getCreatorId(), userId)) {
                result.add(v.getId());
            }
        }

        // 2. 在 contributor 表中且角色为「内容作者 / 协作者」的版本（批量查询，避免 N+1）
        List<Long> versionIds = existed.stream()
                .map(DialogueVersion::getId)
                .collect(Collectors.toList());
        if (!versionIds.isEmpty()) {
            List<Long> contributorVersionIds =
                    dialogueVersionContributorMapper.listEditableVersionIdsByUser(versionIds, userId);
            if (contributorVersionIds != null) {
                result.addAll(contributorVersionIds);
            }
        }

        return result;
    }

    /**
     * 实体 → 基础 VO
     */
    private DialogueVersionVO toBasicVO(DialogueVersion version) {
        if (version == null) return null;
        DialogueVersionVO vo = new DialogueVersionVO();
        BeanUtils.copyProperties(version, vo);
        vo.setSourceTypeLabel(DialogueSourceTypeEnum.getLabel(version.getSourceType()));
        vo.setLanguageLabel(DialogueLanguageEnum.getLabel(version.getLanguage()));
        vo.setFormatLabel(DialogueFormatEnum.getLabel(version.getFormat()));
        vo.setScopeLabel(DialogueScopeEnum.getLabel(version.getScope()));
        return vo;
    }

    /**
     * 加载文字版本内容
     */
    private List<DialogueLineVO> loadLines(Long versionId) {
        List<DialogueLine> lines = dialogueLineMapper.listByVersionId(versionId);
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> lineIds = lines.stream()
                .map(DialogueLine::getId)
                .collect(Collectors.toList());
        List<DialogueSegment> allSegments = dialogueSegmentMapper.listByLineIds(lineIds);

        Map<Long, List<DialogueSegment>> segmentMap = new HashMap<>();
        if (allSegments != null) {
            for (DialogueSegment s : allSegments) {
                segmentMap.computeIfAbsent(s.getLineId(), k -> new ArrayList<>()).add(s);
            }
        }

        List<DialogueLineVO> result = new ArrayList<>(lines.size());
        for (DialogueLine line : lines) {
            DialogueLineVO vo = new DialogueLineVO();
            BeanUtils.copyProperties(line, vo);

            List<DialogueSegment> segments = segmentMap.get(line.getId());
            List<DialogueSegmentVO> segmentVOs = new ArrayList<>();
            if (segments != null) {
                for (DialogueSegment s : segments) {
                    DialogueSegmentVO sVO = new DialogueSegmentVO();
                    BeanUtils.copyProperties(s, sVO);
                    segmentVOs.add(sVO);
                }
            }
            vo.setSegments(segmentVOs);
            result.add(vo);
        }
        return result;
    }

    /**
     * 加载图片版本内容
     */
    private List<DialogueImageVO> loadImages(Long versionId) {
        List<DialogueImage> images = dialogueImageMapper.listByVersionId(versionId);
        if (images == null) {
            return new ArrayList<>();
        }
        List<DialogueImageVO> result = new ArrayList<>(images.size());
        for (DialogueImage img : images) {
            DialogueImageVO vo = new DialogueImageVO();
            BeanUtils.copyProperties(img, vo);
            result.add(vo);
        }
        return result;
    }

    /**
     * 组合 key：language-format-scope
     */
    private String keyOf(Integer language, Integer format, Integer scope) {
        return language + "-" + format + "-" + scope;
    }

    /**
     * 用于广播消息正文的"来源提示"：
     * <ul>
     *   <li>剧情：{@code 剧情《xxx》}</li>
     *   <li>RC / RTV / Rabitter：{@code 卡面名[系列名]·RC 第 N 话}</li>
     * </ul>
     * 找不到来源时回退到 {@code 对话版本 #xxx}，不影响消息发送。
     * <p>
     * 卡面类通过 {@link CardMapper#getWithSeriesById(Long)} 一次拿到卡面名 + 系列名，
     * 避免再查一次 card_series。
     */
    private String buildSourceHint(Integer sourceType, Long sourceId) {
        try {
            if (DialogueSourceTypeEnum.STORY.getValue().equals(sourceType)) {
                Story story = storyMapper.getById(sourceId);
                if (story != null) {
                    return "剧情《" + story.getTitle() + "》";
                }
            } else if (DialogueSourceTypeEnum.RC.getValue().equals(sourceType)) {
                CardRc rc = cardRcMapper.getById(sourceId);
                if (rc != null) {
                    return buildCardEpisodeHint(rc.getCardId(), "RC", rc.getEpisodeNo(), rc.getTitle());
                }
            } else if (DialogueSourceTypeEnum.RTV.getValue().equals(sourceType)) {
                CardRtv rtv = cardRtvMapper.getById(sourceId);
                if (rtv != null) {
                    return buildCardEpisodeHint(rtv.getCardId(), "RTV", rtv.getEpisodeNo(), rtv.getTitle());
                }
            } else if (DialogueSourceTypeEnum.RABITTER.getValue().equals(sourceType)) {
                CardRabitter rabitter = cardRabitterMapper.getById(sourceId);
                if (rabitter != null) {
                    return buildCardEpisodeHint(rabitter.getCardId(), "Rabitter", rabitter.getEpisodeNo(), rabitter.getTitle());
                }
            }
        } catch (Exception e) {
            log.warn("构建来源提示失败：sourceType={}, sourceId={}", sourceType, sourceId, e);
        }
        return "对话版本 #" + sourceId;
    }

    /**
     * 卡面话数的来源提示：{@code 卡面名[系列名]·RC 第 N 话}
     * <p>
     * 卡面不存在或无系列名时降级展示，保证提示始终有值。
     *
     * @param cardId       所属卡面ID
     * @param typeLabel    RC / RTV / Rabitter
     * @param episodeNo    第几话
     * @param episodeTitle 话名
     */
    private String buildCardEpisodeHint(Long cardId, String typeLabel, Integer episodeNo, String episodeTitle) {
        String episodePart = typeLabel + " 第" + (episodeNo == null ? "?" : episodeNo) + "话 " + episodeTitle;
        if (cardId == null) {
            return episodePart;
        }

        CardSourceVO card = cardMapper.getWithSeriesById(cardId);
        if (card == null) {
            return episodePart;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(card.getName() != null && !card.getName().isBlank()
                ? card.getName()
                : "卡面 #" + cardId);
        if (card.getSeriesName() != null && !card.getSeriesName().isBlank()) {
            sb.append("[").append(card.getSeriesName()).append("]");
        }
        sb.append("·").append(episodePart);
        return sb.toString();
    }
}
