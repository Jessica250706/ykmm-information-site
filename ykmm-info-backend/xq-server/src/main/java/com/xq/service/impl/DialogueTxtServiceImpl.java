package com.xq.service.impl;

import com.xq.constant.DialogueConstant;
import com.xq.dto.DialogueLineDTO;
import com.xq.dto.DialogueTxtImportDTO;
import com.xq.entity.DialogueVersion;
import com.xq.entity.Role;
import com.xq.entity.Sticker;
import com.xq.enums.DialogueFormatEnum;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (file.getSize() > DialogueConstant.TXT_MAX_SIZE) {
            throw new RuntimeException("txt 文件大小不能超过 2MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || !originalFilename.toLowerCase()
                .endsWith(DialogueConstant.TXT_EXTENSION)) {
            throw new RuntimeException("只允许上传 .txt 文件");
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        if (!DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            throw new RuntimeException("该版本不是文字版本");
        }

        // 读取内容
        String text = readTxt(file);

        // 逐行解析
        return doParse(text);
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
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        if (dto == null || dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new RuntimeException("导入内容不能为空");
        }
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
            // 去掉 UTF-8 BOM
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
     * 执行解析
     *
     * @param text 文本
     * @return 解析结果
     */
    private DialogueTxtParseVO doParse(String text) {
        List<DialogueLineVO> lines = new ArrayList<>();
        Set<String> speakers = new LinkedHashSet<>();
        Set<String> unmatchedSpeakers = new LinkedHashSet<>();
        Set<String> unmatchedStickers = new LinkedHashSet<>();
        List<String> errors = new ArrayList<>();

        String[] rawLines = text.split("\n");
        String currentSpeaker = null;
        StringBuilder buffer = new StringBuilder();
        int sort = 1;

        for (int i = 0; i < rawLines.length; i++) {
            String raw = rawLines[i].replace("\r", "");
            String line = raw.trim();

            // 跳过空行
            if (line.isEmpty()) {
                continue;
            }
            // 跳过标题行（第一行）和分隔线
            if (line.startsWith("——") || line.matches("^-+$")) {
                continue;
            }

            Matcher m = SPEAKER_PATTERN.matcher(line);
            if (m.matches()) {
                // 说话人切换：先 flush 上一条
                if (currentSpeaker != null) {
                    flushLine(lines, currentSpeaker, buffer.toString(),
                            sort++, speakers, unmatchedSpeakers,
                            unmatchedStickers, errors);
                    buffer.setLength(0);
                }
                currentSpeaker = m.group(1).trim();
                continue;
            }

            // 其他行：追加到当前消息
            if (currentSpeaker == null) {
                // 第一行可能是标题，跳过
                if (i == 0) {
                    continue;
                }
                errors.add("第 " + (i + 1) + " 行缺少说话人：" + line);
                continue;
            }
            buffer.append(line);
        }

        // 最后一条
        if (currentSpeaker != null) {
            flushLine(lines, currentSpeaker, buffer.toString(),
                    sort, speakers, unmatchedSpeakers,
                    unmatchedStickers, errors);
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
     * 处理一条完整消息，生成 DialogueLineVO
     *
     * @param lines             结果列表
     * @param speakerName       说话人名称
     * @param content           内容
     * @param sort              排序
     * @param speakers          已识别说话人集合
     * @param unmatchedSpeakers 未匹配说话人集合
     * @param unmatchedStickers 未匹配表情包集合
     * @param errors            错误列表
     */
    private void flushLine(List<DialogueLineVO> lines,
                           String speakerName,
                           String content,
                           int sort,
                           Set<String> speakers,
                           Set<String> unmatchedSpeakers,
                           Set<String> unmatchedStickers,
                           List<String> errors) {
        if (content == null || content.isEmpty()) {
            return;
        }
        speakers.add(speakerName);

        // 匹配角色
        Role role = matchRole(speakerName);
        Long speakerId = role == null ? null : role.getId();
        if (speakerId == null) {
            unmatchedSpeakers.add(speakerName);
        }

        // 解析片段
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
        // 先按 role.name 精确匹配
        Role role = roleMapper.getByName(speakerName);
        if (role != null) {
            return role;
        }
        // 再按 person.name_cn 反查
        return roleMapper.getByPersonNameCn(speakerName);
    }
}
