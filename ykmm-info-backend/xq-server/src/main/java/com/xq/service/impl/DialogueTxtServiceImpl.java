package com.xq.service.impl;

import com.xq.constant.DialogueConstant;
import com.xq.dto.DialogueTxtImportDTO;
import com.xq.entity.DialogueVersion;
import com.xq.entity.Role;
import com.xq.entity.Sticker;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueRoleEnum;
import com.xq.enums.DialogueSourceTypeEnum;
import com.xq.mapper.DialogueVersionMapper;
import com.xq.mapper.RoleMapper;
import com.xq.mapper.StickerMapper;
import com.xq.service.DialogueLineService;
import com.xq.service.DialogueTxtService;
import com.xq.vo.DialogueLineVO;
import com.xq.vo.DialogueSegmentVO;
import com.xq.vo.DialogueTxtParseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * txt 解析导入服务实现
 */
@Service
@Slf4j
public class DialogueTxtServiceImpl implements DialogueTxtService {

    /**
     * 说话人正则
     */
    private static final Pattern SPEAKER_PATTERN =
            Pattern.compile(DialogueConstant.SPEAKER_PATTERN);

    /**
     * 表情包正则
     */
    private static final Pattern STICKER_PATTERN =
            Pattern.compile(DialogueConstant.STICKER_PATTERN);

    /**
     * 选项编号行
     */
    private static final Pattern OPTION_NUMBER_PATTERN =
            Pattern.compile(DialogueConstant.OPTION_NUMBER_PATTERN);

    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;
    @Autowired
    private DialogueLineService dialogueLineService;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private StickerMapper stickerMapper;

    /**
     * 解析 txt，返回预览
     *
     * @param versionId 版本ID
     * @param file      txt 文件
     * @return 解析结果
     */
    @Override
    public DialogueTxtParseVO parseTxt(Long versionId, MultipartFile file) {
        /* 版本ID不能为空 */
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        /* 文件不能为空 */
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        /* 文件大小限制 */
        if (file.getSize() > DialogueConstant.TXT_MAX_SIZE) {
            throw new RuntimeException("txt 文件大小不能超过 2MB");
        }
        String originalFilename = file.getOriginalFilename();
        /* 必须是 txt 文件 */
        if (originalFilename == null
                || !originalFilename.toLowerCase().endsWith(DialogueConstant.TXT_EXTENSION)) {
            throw new RuntimeException("只允许上传 .txt 文件");
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        /* 版本必须存在 */
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        /* 必须是文字版本 */
        if (!DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            throw new RuntimeException("该版本不是文字版本");
        }

        /* 只有 RC 才解析选项结构 */
        boolean allowOptions = Objects.equals(DialogueSourceTypeEnum.RC.getValue(), version.getSourceType());

        String text = readTxt(file);
        return doParse(text, allowOptions);
    }

    /**
     * 确认导入解析结果
     *
     * @param versionId 版本ID
     * @param dto       确认后的句子
     */
    @Override
    @Transactional
    public void importParsed(Long versionId, DialogueTxtImportDTO dto) {
        /* 版本ID不能为空 */
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        /* 导入内容不能为空 */
        if (dto == null || dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new RuntimeException("导入内容不能为空");
        }

        /* 先清空当前版本下所有对话，避免追加导致重复 */
        dialogueLineService.clearByVersionId(versionId);

        /* 再批量写入新对话（saveBatch 内部会处理 rc_option） */
        dialogueLineService.saveBatch(versionId, dto.getLines());

        log.info("导入 txt 对话成功，versionId={}, count={}",
                versionId, dto.getLines().size());
    }

    // ---------------------------------------------------
    // 私有方法
    // ---------------------------------------------------

    /**
     * 读取 txt 文件内容
     *
     * @param file 文件
     * @return 文本
     */
    private String readTxt(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String text = sb.toString();
            /* 去掉 UTF-8 BOM */
            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            return text;
        } catch (IOException e) {
            log.error("读取 txt 文件失败", e);
            throw new RuntimeException("读取 txt 文件失败");
        }
    }

    /**
     * 按 "——" 切块
     *
     * @param text 原文
     * @return 块列表，每块是若干非空行
     */
    private List<List<String>> splitIntoBlocks(String text) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        String[] rawLines = text.split("\n");
        for (String raw : rawLines) {
            String line = raw.replace("\r", "").trim();
            if (line.isEmpty()) {
                continue;
            }
            /* 分隔线：结束当前块，开启下一块 */
            if (line.startsWith("——") || line.matches("^-+$")) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                continue;
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            blocks.add(current);
        }
        return blocks;
    }

    /**
     * 执行解析
     *
     * @param text         文本
     * @param allowOptions 是否解析选项结构（仅 RC）
     * @return 解析结果
     */
    private DialogueTxtParseVO doParse(String text, boolean allowOptions) {
        List<DialogueLineVO> lines = new ArrayList<>();
        Set<String> speakers = new LinkedHashSet<>();
        Set<String> unmatchedSpeakers = new LinkedHashSet<>();
        Set<String> unmatchedStickers = new LinkedHashSet<>();
        List<String> errors = new ArrayList<>();

        List<List<String>> blocks = splitIntoBlocks(text);
        int sort = 1;

        for (int b = 0; b < blocks.size(); b++) {
            List<String> block = blocks.get(b);

            /**
             * 是否是选项块：
             * - 必须允许解析选项（RC 版本）
             * - 块内第一行必须匹配 "N." 编号行
             */
            Integer optionNumber = detectOptionNumber(block);
            boolean isOption = allowOptions && optionNumber != null;

            sort = parseBlock(block, isOption, optionNumber, sort,
                    lines, speakers, unmatchedSpeakers, unmatchedStickers, errors);
        }

        return DialogueTxtParseVO.builder()
                .speakers(new ArrayList<>(speakers))
                .unmatchedSpeakers(new ArrayList<>(unmatchedSpeakers))
                .unmatchedStickers(new ArrayList<>(unmatchedStickers))
                .lines(lines)
                .errors(errors)
                .build();
    }

    /**
     * 检测块是否为选项块
     *
     * 规则：块内第一个非空行必须是 "N." 形式的编号行。
     * 如果是，解析出编号 N 并返回；否则返回 null。
     *
     * @param block 块内行
     * @return 选项编号，非选项块返回 null
     */
    private Integer detectOptionNumber(List<String> block) {
        if (block == null || block.isEmpty()) {
            return null;
        }
        /** 第一行必须是编号行 */
        String first = block.get(0);
        if (!OPTION_NUMBER_PATTERN.matcher(first).matches()) {
            return null;
        }
        /** 取出 "N." 里的 N */
        try {
            String numStr = first.substring(0, first.length() - 1).trim();
            int n = Integer.parseInt(numStr);
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析一个块
     *
     * @param block             块内行
     * @param isOption          是否选项块
     * @param optionNumber      选项编号
     * @param startSort         起始 sort
     * @param lines             结果列表
     * @param speakers          说话人集合
     * @param unmatchedSpeakers 未匹配说话人
     * @param unmatchedStickers 未匹配表情包
     * @param errors            错误列表
     * @return 下一个可用的 sort
     */
    private int parseBlock(List<String> block,
                           boolean isOption,
                           Integer optionNumber,
                           int startSort,
                           List<DialogueLineVO> lines,
                           Set<String> speakers,
                           Set<String> unmatchedSpeakers,
                           Set<String> unmatchedStickers,
                           List<String> errors) {
        String currentSpeaker = null;
        StringBuilder buffer = new StringBuilder();
        int stage = 0;   // 0=问句 1=回答（仅选项块有意义）
        int sort = startSort;

        for (String line : block) {
            /* 选项块内，忽略 "N." 编号行 */
            if (isOption && OPTION_NUMBER_PATTERN.matcher(line).matches()) {
                continue;
            }

            Matcher m = SPEAKER_PATTERN.matcher(line);
            if (m.matches()) {
                if (currentSpeaker != null && !buffer.isEmpty()) {
                    DialogueRoleEnum role = isOption
                            ? (stage == 0 ? DialogueRoleEnum.QUESTION : DialogueRoleEnum.ANSWER)
                            : DialogueRoleEnum.NORMAL;
                    flushLine(lines, currentSpeaker, buffer.toString(), sort++,
                            role.getValue(), isOption ? optionNumber : null,
                            speakers, unmatchedSpeakers, unmatchedStickers, errors);
                    if (isOption) {
                        stage++;
                    }
                    buffer.setLength(0);
                }
                currentSpeaker = m.group(1).trim();
                continue;
            }

            /* 普通内容行：追加到当前说话人缓冲 */
            if (currentSpeaker == null) {
                errors.add("块内缺少说话人：" + line);
                continue;
            }
            if (!buffer.isEmpty()) {
                buffer.append("\n");
            }
            buffer.append(line);
        }

        /* 块末尾的最后一条 */
        if (currentSpeaker != null && !buffer.isEmpty()) {
            DialogueRoleEnum role = isOption
                    ? (stage == 0 ? DialogueRoleEnum.QUESTION : DialogueRoleEnum.ANSWER)
                    : DialogueRoleEnum.NORMAL;
            flushLine(lines, currentSpeaker, buffer.toString(), sort++,
                    role.getValue(), isOption ? optionNumber : null,
                    speakers, unmatchedSpeakers, unmatchedStickers, errors);
        }

        return sort;
    }

    /**
     * 处理一条完整消息，生成 DialogueLineVO
     *
     * @param lines             结果列表
     * @param speakerName       说话人名称
     * @param content           内容
     * @param sort              排序
     * @param dialogueRole      对话角色
     * @param optionNumber      选项编号（普通行传 null）
     * @param speakers          已识别说话人集合
     * @param unmatchedSpeakers 未匹配说话人集合
     * @param unmatchedStickers 未匹配表情包集合
     * @param errors            错误列表
     */
    private void flushLine(List<DialogueLineVO> lines,
                           String speakerName,
                           String content,
                           int sort,
                           int dialogueRole,
                           Integer optionNumber,
                           Set<String> speakers,
                           Set<String> unmatchedSpeakers,
                           Set<String> unmatchedStickers,
                           List<String> errors) {
        if (content == null || content.isEmpty()) {
            return;
        }
        speakers.add(speakerName);

        /* 匹配角色 */
        Role role = matchRole(speakerName);
        Long speakerId = role == null ? null : role.getId();
        if (speakerId == null) {
            unmatchedSpeakers.add(speakerName);
        }

        /* 解析片段 */
        List<DialogueSegmentVO> segments = new ArrayList<>();
        Matcher matcher = STICKER_PATTERN.matcher(content);
        int lastEnd = 0;
        int segSort = 1;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                DialogueSegmentVO seg = new DialogueSegmentVO();
                seg.setSegmentType(1);
                seg.setContent(content.substring(lastEnd, matcher.start()));
                seg.setSort(segSort++);
                segments.add(seg);
            }
            String label = matcher.group(1);
            Sticker sticker = stickerMapper.getByLabel(label);
            DialogueSegmentVO seg = new DialogueSegmentVO();
            seg.setSegmentType(2);
            seg.setStickerLabel(label);
            if (sticker != null) {
                seg.setStickerId(sticker.getId());
                seg.setStickerImageUrl(sticker.getImageUrl());
                seg.setStickerEmoji(sticker.getEmoji());
            } else {
                unmatchedStickers.add(label);
            }
            seg.setSort(segSort++);
            segments.add(seg);
            lastEnd = matcher.end();
        }
        if (lastEnd < content.length()) {
            DialogueSegmentVO seg = new DialogueSegmentVO();
            seg.setSegmentType(1);
            seg.setContent(content.substring(lastEnd));
            seg.setSort(segSort);
            segments.add(seg);
        }

        DialogueLineVO vo = DialogueLineVO.builder()
                .speakerId(speakerId)
                .speakerName(speakerName)
                .personId(role == null ? null : role.getPersonId())
                .content(content)
                .sort(sort)
                .dialogueRole(dialogueRole)
                .optionNumber(optionNumber)
                .segments(segments)
                .build();
        lines.add(vo);
    }

    /**
     * 匹配角色
     *
     * @param speakerName 说话人名称
     * @return 角色，未匹配返回 null
     */
    private Role matchRole(String speakerName) {
        if (speakerName == null || speakerName.isBlank()) {
            return null;
        }
        /* 先按 role.name 精确匹配 */
        Role role = roleMapper.getByName(speakerName);
        if (role != null) {
            return role;
        }
        /* 再按 person.name_cn 反查 */
        return roleMapper.getByPersonNameCn(speakerName);
    }
}
