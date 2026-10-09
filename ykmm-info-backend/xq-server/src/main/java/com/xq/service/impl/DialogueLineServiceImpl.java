package com.xq.service.impl;

import com.xq.constant.DialogueConstant;
import com.xq.dto.DialogueLineDTO;
import com.xq.entity.*;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueSegmentTypeEnum;
import com.xq.enums.DialogueSideEnum;
import com.xq.enums.DialogueSourceTypeEnum;
import com.xq.mapper.*;
import com.xq.service.DialogueLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对话句子服务实现
 */
@Service
@Slf4j
public class DialogueLineServiceImpl implements DialogueLineService {

    /**
     * 表情包标签正则
     */
    private static final Pattern STICKER_PATTERN =
            Pattern.compile(DialogueConstant.STICKER_PATTERN);

    @Autowired
    private DialogueLineMapper dialogueLineMapper;

    @Autowired
    private DialogueSegmentMapper dialogueSegmentMapper;

    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private StickerMapper stickerMapper;

    @Autowired
    private CardRcMapper cardRcMapper;

    @Autowired
    private RcOptionMapper rcOptionMapper;

    /**
     * 批量保存句子
     *
     * @param versionId 版本ID
     * @param lines     句子列表
     */
    @Override
    @Transactional
    public void saveBatch(Long versionId, List<DialogueLineDTO> lines) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        if (lines == null || lines.isEmpty()) {
            throw new RuntimeException("句子列表不能为空");
        }
        if (lines.size() > DialogueConstant.MAX_BATCH_SIZE) {
            throw new RuntimeException("单次保存句子数量不能超过 "
                    + DialogueConstant.MAX_BATCH_SIZE);
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        if (!DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            throw new RuntimeException("该版本不是文字版本");
        }

        // ★ RC 场景下：先查 card_rc 拿到发起人角色ID
        Long initiatorRoleId = resolveInitiatorRoleId(version);

        // 计算起始 sort
        Integer maxSort = dialogueLineMapper.getMaxSort(versionId);
        int startSort = (maxSort == null ? 0 : maxSort) + 1;

        // 构建实体列表
        List<DialogueLine> entityList = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            DialogueLineDTO dto = lines.get(i);
            validateLineDTO(dto, version);

            DialogueLine line = new DialogueLine();
            BeanUtils.copyProperties(dto, line);
            line.setSpeakerId(dto.getSpeakerId());
            line.setSide(dto.getSide());
            line.setVersionId(versionId);
            line.setSort(dto.getSort() != null ? dto.getSort() :startSort + i);
            line.setMonologue(dto.getMonologue() != null ? dto.getMonologue() : 0);

            // ★ 计算 side
            line.setSide(resolveSide(dto.getSpeakerId(), dto.getSide(), initiatorRoleId));

            entityList.add(line);
        }

        // 批量插入
        dialogueLineMapper.insertBatch(entityList);

        // 解析每条句子的片段
        List<DialogueSegment> allSegments = new ArrayList<>();
        for (DialogueLine line : entityList) {
            List<DialogueSegment> segments = parseSegments(line.getId(), line.getContent());
            allSegments.addAll(segments);
        }
        if (!allSegments.isEmpty()) {
            dialogueSegmentMapper.insertBatch(allSegments);
        }

        log.info("批量保存对话句子成功，versionId={}, count={}, initiatorRoleId={}",
                versionId, entityList.size(), initiatorRoleId);
    }

    /**
     * 编辑单句
     *
     * @param lineId 句子ID
     * @param dto    编辑参数
     */
    @Override
    @Transactional
    public void update(Long lineId, DialogueLineDTO dto) {
        if (lineId == null) {
            throw new RuntimeException("句子ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        DialogueLine exist = dialogueLineMapper.getById(lineId);
        if (exist == null) {
            throw new RuntimeException("对话句子不存在");
        }
        DialogueVersion version = dialogueVersionMapper.getById(exist.getVersionId());
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        validateLineDTO(dto, version);

        DialogueLine line = new DialogueLine();
        BeanUtils.copyProperties(dto, line);
        line.setId(lineId);
        dialogueLineMapper.update(line);

        // 重建片段
        if (dto.getContent() != null) {
            dialogueSegmentMapper.deleteByLineId(lineId);
            List<DialogueSegment> segments =
                    parseSegments(lineId, dto.getContent());
            if (!segments.isEmpty()) {
                dialogueSegmentMapper.insertBatch(segments);
            }
        }
        log.info("编辑对话句子成功，id={}", lineId);
    }

    /**
     * 删除单句
     *
     * @param lineId 句子ID
     */
    @Override
    @Transactional
    public void delete(Long lineId) {
        if (lineId == null) {
            throw new RuntimeException("句子ID不能为空");
        }
        DialogueLine exist = dialogueLineMapper.getById(lineId);
        if (exist == null) {
            throw new RuntimeException("对话句子不存在");
        }
        dialogueSegmentMapper.deleteByLineId(lineId);
        dialogueLineMapper.deleteById(lineId);
        log.info("删除对话句子成功，id={}", lineId);
    }

    /**
     * 调整句子顺序
     *
     * @param versionId 版本ID
     * @param lineIds   句子ID顺序
     */
    @Override
    @Transactional
    public void sort(Long versionId, List<Long> lineIds) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        if (lineIds == null || lineIds.isEmpty()) {
            throw new RuntimeException("句子ID列表不能为空");
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        for (int i = 0; i < lineIds.size(); i++) {
            Long lineId = lineIds.get(i);
            DialogueLine line = dialogueLineMapper.getById(lineId);
            if (line == null) {
                throw new RuntimeException("对话句子不存在：id=" + lineId);
            }
            if (!line.getVersionId().equals(versionId)) {
                throw new RuntimeException("句子不属于该版本：id=" + lineId);
            }
            dialogueLineMapper.updateSort(lineId, i + 1);
        }
        log.info("调整对话句子顺序成功，versionId={}", versionId);
    }

    /**
     * 清空指定版本下所有对话句子及其关联数据
     * 用于 txt 导入前的覆盖操作
     *
     * @param versionId 版本ID
     */
    @Override
    @Transactional
    public void clearByVersionId(Long versionId) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }

        /** 1. 查出该版本下所有句子ID */
        List<Long> lineIds = dialogueLineMapper.listIdsByVersionId(versionId);
        if (lineIds == null || lineIds.isEmpty()) {
            log.info("清空对话：版本 {} 下无句子，跳过", versionId);
            return;
        }

        /** 2. 删除片段 */
        dialogueSegmentMapper.deleteByLineIds(lineIds);

        /** 3. 删除引用这些句子的 RC 选项 */
        rcOptionMapper.deleteByLineIds(lineIds);

        /** 4. 删除句子本身 */
        dialogueLineMapper.deleteByVersionId(versionId);

        log.info("清空版本对话成功，versionId={}, lineCount={}", versionId, lineIds.size());
    }

    // ---------------------------------------------------
    // 私有方法
    // ---------------------------------------------------

    /**
     * 校验句子 DTO
     *
     * @param dto     句子
     * @param version 版本
     */
    private void validateLineDTO(DialogueLineDTO dto, DialogueVersion version) {
        if (dto == null) {
            throw new RuntimeException("句子不能为空");
        }
        if (dto.getSpeakerId() == null) {
            throw new RuntimeException("说话角色不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new RuntimeException("对话内容不能为空");
        }
        if (dto.getContent().length() > DialogueConstant.MAX_CONTENT_LENGTH) {
            throw new RuntimeException("对话内容长度不能超过 "
                    + DialogueConstant.MAX_CONTENT_LENGTH);
        }
        Role role = roleMapper.getById(dto.getSpeakerId());
        if (role == null) {
            throw new RuntimeException("说话角色不存在：id=" + dto.getSpeakerId());
        }
        // RC 场景下 side 由服务端按发起人自动计算，前端可选传；
        // 只要传了就必须是 1 或 2
        if (dto.getSide() != null && !DialogueSideEnum.isValid(dto.getSide())) {
            throw new RuntimeException("side 取值不合法：" + dto.getSide());
        }
    }

    /**
     * 解析文本片段，识别表情包标签
     *
     * @param lineId  句子ID
     * @param content 原始文本
     * @return 片段列表
     */
    private List<DialogueSegment> parseSegments(Long lineId, String content) {
        List<DialogueSegment> result = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return result;
        }
        Matcher matcher = STICKER_PATTERN.matcher(content);
        int lastEnd = 0;
        int sort = 1;

        while (matcher.find()) {
            // 文本片段
            if (matcher.start() > lastEnd) {
                String text = content.substring(lastEnd, matcher.start());
                if (!text.isEmpty()) {
                    DialogueSegment seg = new DialogueSegment();
                    seg.setLineId(lineId);
                    seg.setSegmentType(DialogueSegmentTypeEnum.TEXT.getValue());
                    seg.setContent(text);
                    seg.setSort(sort++);
                    result.add(seg);
                }
            }
            // 表情包片段
            String label = matcher.group(1);
            Sticker sticker = stickerMapper.getByLabel(label);
            DialogueSegment stickerSeg = new DialogueSegment();
            stickerSeg.setLineId(lineId);
            stickerSeg.setSegmentType(DialogueSegmentTypeEnum.STICKER.getValue());
            stickerSeg.setStickerId(sticker == null ? null : sticker.getId());
            stickerSeg.setContent(sticker == null ? "[" + label + "]" : null);
            stickerSeg.setSort(sort++);
            result.add(stickerSeg);

            lastEnd = matcher.end();
        }
        // 尾部文本
        if (lastEnd < content.length()) {
            String text = content.substring(lastEnd);
            if (!text.isEmpty()) {
                DialogueSegment seg = new DialogueSegment();
                seg.setLineId(lineId);
                seg.setSegmentType(DialogueSegmentTypeEnum.TEXT.getValue());
                seg.setContent(text);
                seg.setSort(sort);
                result.add(seg);
            }
        }
        return result;
    }

    /**
     * 解析 RC 发起人角色ID
     *
     * - 来源类型为 RC 时：查 card_rc 拿 role_id 作为发起人
     * - 其他来源：返回 null
     *
     * @param version 对话版本
     * @return 发起人角色ID，非 RC 返回 null
     */
    private Long resolveInitiatorRoleId(DialogueVersion version) {
        if (version == null || version.getSourceType() == null) {
            return null;
        }
        if (!(Objects.equals(DialogueSourceTypeEnum.RC.getValue(), version.getSourceType()))) {
            return null;
        }
        if (version.getSourceId() == null) {
            throw new RuntimeException("RC 版本的来源ID不能为空");
        }
        CardRc rc = cardRcMapper.getById(version.getSourceId());
        if (rc == null) {
            throw new RuntimeException("RC 不存在：id=" + version.getSourceId());
        }
        if (rc.getRoleId() == null) {
            throw new RuntimeException("RC 未设置发起人角色");
        }
        return rc.getRoleId();
    }

    /**
     * 计算对话句子的左右位置
     *
     * 规则：
     * - RC 场景（initiatorRoleId != null）：
     *     speakerId == initiatorRoleId → 2（右侧）
     *     其他 → 1（左侧）
     * - 非 RC 场景：使用前端传入的 side，可为 null
     *
     * @param speakerId       说话角色ID
     * @param sideFromRequest 前端传入的 side
     * @param initiatorRoleId RC发起人角色ID
     * @return 最终 side
     */
    private Integer resolveSide(Long speakerId, Integer sideFromRequest, Long initiatorRoleId) {
        if (initiatorRoleId == null) {
            // 非 RC：沿用前端值
            return sideFromRequest;
        }
        return Objects.equals(speakerId, initiatorRoleId)
                ? DialogueSideEnum.RIGHT.getValue()
                : DialogueSideEnum.LEFT.getValue();
    }
}
