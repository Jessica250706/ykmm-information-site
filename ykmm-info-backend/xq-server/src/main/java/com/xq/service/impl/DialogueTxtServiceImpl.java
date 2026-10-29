package com.xq.service.impl;

import com.xq.constant.DialogueConstant;
import com.xq.constant.DialogueSegmentTypeConstant;
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

@Service
@Slf4j
public class DialogueTxtServiceImpl implements DialogueTxtService {

    private static final Pattern SPEAKER_PATTERN =
            Pattern.compile(DialogueConstant.SPEAKER_PATTERN);

    private static final Pattern STICKER_PATTERN =
            Pattern.compile(DialogueConstant.STICKER_PATTERN);

    private static final Pattern OPTION_NUMBER_PATTERN =
            Pattern.compile(DialogueConstant.OPTION_NUMBER_PATTERN);

    /** 纯省略号行（…… 或 ... ） */
    private static final Pattern ELLIPSIS_ONLY_PATTERN =
            Pattern.compile("^(…+|\\.{3,})$");

    /**
     * role.name 字段最大长度（表定义 VARCHAR(64)）。
     * 自动创建角色时若姓名超长，会截断到该长度内。
     */
    private static final int ROLE_NAME_MAX_LENGTH = 60;

    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;
    @Autowired
    private DialogueLineService dialogueLineService;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private StickerMapper stickerMapper;

    @Override
    public DialogueTxtParseVO parseTxt(Long versionId, MultipartFile file) {
        if (file.getSize() > DialogueConstant.TXT_MAX_SIZE) {
            throw new RuntimeException("txt 文件大小不能超过 2MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || !originalFilename.toLowerCase().endsWith(DialogueConstant.TXT_EXTENSION)) {
            throw new RuntimeException("只允许上传 .txt 文件");
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        if (!DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            throw new RuntimeException("该版本不是文字版本");
        }

        boolean allowOptions = Objects.equals(
                DialogueSourceTypeEnum.RC.getValue(), version.getSourceType());

        String text = readTxt(file);
        return doParse(text, allowOptions);
    }

    @Override
    @Transactional
    public void importParsed(Long versionId, DialogueTxtImportDTO dto) {
        dialogueLineService.clearByVersionId(versionId);
        dialogueLineService.saveBatch(versionId, dto.getLines());
        log.info("导入 txt 对话成功，versionId={}, count={}", versionId, dto.getLines().size());
    }

    // ---------------------------------------------------
    // 私有方法
    // ---------------------------------------------------

    private String readTxt(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String text = sb.toString();
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
     * 按「空行」和「——分隔线」切块。
     * <p>
     * 空行也是切分点：这样「……」「【音效】」「（动作）」这类独立成段的旁白
     * 才能从上一个说话人的内容里剥离出来，成为独立块。
     * <p>
     * 「——」分隔符只匹配"整行就是 ——/---"，避免误删以破折号开头的内容行（如「——克斯！」）。
     */
    private List<List<String>> splitIntoBlocks(String text) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        String[] rawLines = text.split("\n");
        for (String raw : rawLines) {
            String line = raw.replace("\r", "").trim();

            if (line.isEmpty()) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                continue;
            }

            if (line.equals("——") || line.matches("^-{3,}$")) {
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

    private DialogueTxtParseVO doParse(String text, boolean allowOptions) {
        List<DialogueLineVO> lines = new ArrayList<>();
        Set<String> speakers = new LinkedHashSet<>();
        Set<String> unmatchedSpeakers = new LinkedHashSet<>();
        Set<String> unmatchedStickers = new LinkedHashSet<>();
        List<String> errors = new ArrayList<>();

        /*
         * 本次解析内的角色缓存：speakerName → Role
         * 作用：
         *   1. 同一份 txt 里同一人名只查库一次
         *   2. 自动创建的角色直接放进缓存，避免重复创建
         * 生命周期 = 本次 doParse 调用，不跨请求、不需要清理。
         */
        Map<String, Role> roleCache = new HashMap<>();

        // 旁白角色：同样"查不到就建"，不再依赖手动预插 SQL
        Role narratorRole = matchOrCreateRole(
                DialogueConstant.NARRATOR_ROLE_NAME, roleCache, errors);
        if (narratorRole == null) {
            errors.add("旁白角色创建失败，所有旁白类句子将被跳过");
        }

        List<List<String>> blocks = splitIntoBlocks(text);
        int sort = 1;

        for (List<String> block : blocks) {
            Integer optionNumber = detectOptionNumber(block);
            boolean isOption = allowOptions && optionNumber != null;

            sort = parseBlock(block, isOption, optionNumber, sort, narratorRole, roleCache,
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

    private Integer detectOptionNumber(List<String> block) {
        if (block == null || block.isEmpty()) {
            return null;
        }
        String first = block.get(0);
        if (!OPTION_NUMBER_PATTERN.matcher(first).matches()) {
            return null;
        }
        try {
            String numStr = first.substring(0, first.length() - 1).trim();
            int n = Integer.parseInt(numStr);
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析一个块。
     * <p>
     * 无说话人的旁白行（【...】、纯省略号、（）包裹）会落成独立 line，
     * 说话人统一使用「旁白」角色。
     */
    private int parseBlock(List<String> block,
                           boolean isOption,
                           Integer optionNumber,
                           int startSort,
                           Role narratorRole,
                           Map<String, Role> roleCache,
                           List<DialogueLineVO> lines,
                           Set<String> speakers,
                           Set<String> unmatchedSpeakers,
                           Set<String> unmatchedStickers,
                           List<String> errors) {
        String currentSpeaker = null;
        StringBuilder buffer = new StringBuilder();
        int stage = 0;
        int sort = startSort;

        for (String line : block) {
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
                            roleCache, speakers, unmatchedSpeakers, unmatchedStickers, errors);
                    if (isOption) {
                        stage++;
                    }
                    buffer.setLength(0);
                }
                currentSpeaker = m.group(1).trim();
                continue;
            }

            // 非说话人行
            if (currentSpeaker != null) {
                if (buffer.length() > 0) {
                    buffer.append("\n");
                }
                buffer.append(line);
                continue;
            }

            // 无说话人 + 无缓冲：判定是否是旁白行
            if (isNarratorLine(line)) {
                if (isOption) {
                    errors.add("选项块内不应出现旁白行：" + line);
                } else if (narratorRole == null) {
                    errors.add("旁白角色不存在，跳过旁白行：" + line);
                } else {
                    flushNarratorLine(lines, line, sort++, narratorRole,
                            unmatchedStickers, errors);
                }
            } else {
                errors.add("块内缺少说话人：" + line);
            }
        }

        // 块末尾
        if (currentSpeaker != null && !buffer.isEmpty()) {
            DialogueRoleEnum role = isOption
                    ? (stage == 0 ? DialogueRoleEnum.QUESTION : DialogueRoleEnum.ANSWER)
                    : DialogueRoleEnum.NORMAL;
            flushLine(lines, currentSpeaker, buffer.toString(), sort++,
                    role.getValue(), isOption ? optionNumber : null,
                    roleCache, speakers, unmatchedSpeakers, unmatchedStickers, errors);
        }

        return sort;
    }

    /**
     * 判定一行是否是「旁白行」：
     * <ul>
     *   <li>整行被 {@code 【...】} 包裹（音效、系统提示）</li>
     *   <li>整行只有省略号（{@code ……} 或 {@code ...}）</li>
     *   <li>整行被全/半角圆括号包裹（动作描写）</li>
     * </ul>
     */
    private boolean isNarratorLine(String line) {
        if (line == null) return false;
        String t = line.trim();
        if (t.isEmpty()) return false;

        if (t.startsWith("【") && t.endsWith("】") && t.length() >= 4) {
            return true;
        }
        if (ELLIPSIS_ONLY_PATTERN.matcher(t).matches()) {
            return true;
        }
        if (t.length() >= 4
                && ((t.startsWith("（") && t.endsWith("）"))
                || (t.startsWith("(") && t.endsWith(")")))) {
            return true;
        }
        return false;
    }

    /**
     * 判定内容是否是"整段被一对括号包裹"，即内心独白。
     * <p>
     * 全角括号与半角括号必须"首尾配对"：
     * <ul>
     *   <li>{@code （...）} 视为独白</li>
     *   <li>{@code (...)} 视为独白</li>
     *   <li>{@code （...)} 或 {@code (...）} 混用不视为独白（避免误判）</li>
     * </ul>
     */
    private boolean isMonologueContent(String content) {
        if (content == null) return false;
        String t = content.trim();
        if (t.length() < 4) return false;
        return (t.startsWith("（") && t.endsWith("）"))
                || (t.startsWith("(") && t.endsWith(")"));
    }

    /**
     * 去掉整段包裹的最外层括号。
     * <p>
     * 前置条件：调用方已通过 {@link #isMonologueContent(String)} 判断过括号配对。
     * 本方法只做"首尾各去掉一个字符"，中间内容原样保留。
     * <p>
     * 例如：
     * <ul>
     *   <li>{@code （我的随身项链也带了……好！）} → {@code 我的随身项链也带了……好！}</li>
     *   <li>{@code （不！不是应该！\n是绝对要找到。）} → {@code 不！不是应该！\n是绝对要找到。}</li>
     * </ul>
     * 若去掉后内容为空字符串（例如括号内只有空白），返回原内容，避免产生空对话。
     */
    private String stripOuterBrackets(String content) {
        if (content == null) return null;
        String t = content.trim();
        if (t.length() < 2) return content;

        String inner;
        if (t.startsWith("（") && t.endsWith("）")) {
            inner = t.substring(1, t.length() - 1).trim();
        } else if (t.startsWith("(") && t.endsWith(")")) {
            inner = t.substring(1, t.length() - 1).trim();
        } else {
            // 不满足配对（理论上不应进到这里），原样返回
            return content;
        }

        // 括号内全为空白（如「（  ）」）时，保留原样，避免生成空对话
        return inner.isEmpty() ? content : inner;
    }

    /**
     * 处理一条完整的有角色消息。
     * <p>
     * 若整段内容被括号包裹（内心独白），则：
     * <ul>
     *   <li>{@code monologue = 1}</li>
     *   <li>存储的 {@code content} 去掉首尾括号</li>
     *   <li>片段（segments）也基于去括号后的内容解析</li>
     * </ul>
     */
    private void flushLine(List<DialogueLineVO> lines,
                           String speakerName,
                           String content,
                           int sort,
                           int dialogueRole,
                           Integer optionNumber,
                           Map<String, Role> roleCache,
                           Set<String> speakers,
                           Set<String> unmatchedSpeakers,
                           Set<String> unmatchedStickers,
                           List<String> errors) {
        if (content == null || content.isEmpty()) {
            return;
        }
        speakers.add(speakerName);

        // 内心独白判定 + 去括号
        boolean monologue = isMonologueContent(content);
        String finalContent = monologue ? stripOuterBrackets(content) : content;

        // 查不到就建（利用 roleCache 去重）
        Role role = matchOrCreateRole(speakerName, roleCache, errors);
        Long speakerId = role == null ? null : role.getId();
        if (speakerId == null) {
            unmatchedSpeakers.add(speakerName);
        }

        // 基于最终内容（去括号后）解析片段
        List<DialogueSegmentVO> segments = parseSegments(finalContent, unmatchedStickers);

        DialogueLineVO vo = DialogueLineVO.builder()
                .speakerId(speakerId)
                .speakerName(speakerName)
                .personId(role == null ? null : role.getPersonId())
                .content(finalContent)
                .sort(sort)
                .dialogueRole(dialogueRole)
                .optionNumber(optionNumber)
                .monologue(monologue ? 1 : 0)
                .segments(segments)
                .build();
        lines.add(vo);
    }

    /**
     * 处理一条旁白消息。
     * <p>
     * 注意：旁白保留原有括号（如「（霍普的立绘移动到克斯旁边）」），
     * 因为括号本身就是旁白内容的一部分，去掉会丢失语义。
     */
    private void flushNarratorLine(List<DialogueLineVO> lines,
                                   String content,
                                   int sort,
                                   Role narratorRole,
                                   Set<String> unmatchedStickers,
                                   List<String> errors) {
        if (content == null || content.isBlank()) {
            return;
        }

        // 旁白保留原括号，不做 stripOuterBrackets
        List<DialogueSegmentVO> segments = parseSegments(content, unmatchedStickers);

        DialogueLineVO vo = DialogueLineVO.builder()
                .speakerId(narratorRole.getId())
                .speakerName(narratorRole.getName())
                .personId(null)
                .content(content)
                .sort(sort)
                .dialogueRole(DialogueRoleEnum.NORMAL.getValue())
                .optionNumber(null)
                .monologue(0)
                .segments(segments)
                .build();
        lines.add(vo);
    }

    /**
     * 匹配或创建角色。
     * <p>
     * 查找顺序：
     * <ol>
     *   <li>本地缓存（本次解析已处理过的名字）</li>
     *   <li>role.name 精确匹配</li>
     *   <li>person.name_cn 反查（"千" → 千 的角色）</li>
     *   <li>都没有 → <b>自动创建</b>一个新的 role（person_id = null）</li>
     * </ol>
     * <p>
     * 自动创建的角色会带有 `intro = "由 txt 导入自动创建"` 标记，
     * 方便管理员后续在管理端补充人物关联、简介等信息。
     * <p>
     * 并发说明：单机串行调用，同一请求内通过缓存去重。
     * 若未来多机部署 + 高并发导入，可能因 role.name 无唯一索引而出现重复角色，
     * 届时可加唯一索引或做"插入前二次查询"兜底。
     */
    private Role matchOrCreateRole(String speakerName,
                                   Map<String, Role> roleCache,
                                   List<String> errors) {
        if (speakerName == null || speakerName.isBlank()) {
            return null;
        }
        String key = speakerName.trim();

        // 1. 缓存命中
        Role cached = roleCache.get(key);
        if (cached != null) {
            return cached;
        }

        // 2. 查库
        Role role = roleMapper.getByName(key);
        if (role == null) {
            role = roleMapper.getByPersonNameCn(key);
        }

        // 3. 查不到 → 自动创建
        if (role == null) {
            role = createRole(key, errors);
        }

        if (role != null) {
            roleCache.put(key, role);
        }
        return role;
    }

    /**
     * 自动创建角色。
     * <p>
     * - person_id = null（不关联具体人物，管理员可后续补充）
     * - intro 标注"由 txt 导入自动创建"，便于识别、清理
     * - name 超长时截断到 VARCHAR(64) 安全范围，避免插入失败
     */
    private Role createRole(String name, List<String> errors) {
        String safeName = name;
        if (safeName.length() > ROLE_NAME_MAX_LENGTH) {
            safeName = safeName.substring(0, ROLE_NAME_MAX_LENGTH);
            log.warn("自动创建角色：名称超长已截断，原名={}", name);
        }
        try {
            Role role = new Role();
            role.setName(safeName);
            role.setPersonId(null);
            role.setIntro("由 txt 导入自动创建");
            roleMapper.insert(role);
            log.info("自动创建角色成功：name={}, id={}", safeName, role.getId());
            return role;
        } catch (Exception e) {
            log.error("自动创建角色失败：name={}", safeName, e);
            errors.add("自动创建角色失败：" + safeName + "（" + e.getMessage() + "）");
            return null;
        }
    }

    /**
     * 把一段内容解析成片段（文本 + 表情包）。
     */
    private List<DialogueSegmentVO> parseSegments(String content, Set<String> unmatchedStickers) {
        List<DialogueSegmentVO> segments = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return segments;
        }
        Matcher matcher = STICKER_PATTERN.matcher(content);
        int lastEnd = 0;
        int segSort = 1;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                DialogueSegmentVO seg = new DialogueSegmentVO();
                seg.setSegmentType(DialogueSegmentTypeConstant.TEXT);
                seg.setContent(content.substring(lastEnd, matcher.start()));
                seg.setSort(segSort++);
                segments.add(seg);
            }
            String label = matcher.group(1);
            Sticker sticker = stickerMapper.getByLabel(label);
            DialogueSegmentVO seg = new DialogueSegmentVO();
            seg.setSegmentType(DialogueSegmentTypeConstant.STICKER);
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
            seg.setSegmentType(DialogueSegmentTypeConstant.TEXT);
            seg.setContent(content.substring(lastEnd));
            seg.setSort(segSort);
            segments.add(seg);
        }
        return segments;
    }
}
