package com.xq.service.impl;

import com.xq.constant.DialogueConstant;
import com.xq.constant.DialogueSegmentTypeConstant;
import com.xq.dto.DialogueTxtImportDTO;
import com.xq.entity.*;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueRoleEnum;
import com.xq.enums.DialogueSourceTypeEnum;
import com.xq.mapper.*;
import com.xq.service.DialogueLineService;
import com.xq.service.DialogueTxtService;
import com.xq.vo.DialogueLineVO;
import com.xq.vo.DialogueSegmentVO;
import com.xq.vo.DialogueTxtParseVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DialogueTxtServiceImpl implements DialogueTxtService {

    private static final Pattern SPEAKER_PATTERN =
            Pattern.compile(DialogueConstant.SPEAKER_PATTERN);

    private static final Pattern STICKER_PATTERN =
            Pattern.compile(DialogueConstant.STICKER_PATTERN);

    private static final Pattern OPTION_NUMBER_PATTERN =
            Pattern.compile(DialogueConstant.OPTION_NUMBER_PATTERN);

    /**
     * 纯省略号行（…… 或 ... ）
     */
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
    @Autowired
    private DialogueLineMapper dialogueLineMapper;
    @Autowired
    private RcOptionMapper rcOptionMapper;

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

    @Override
    public void exportTxt(Long versionId, HttpServletResponse response) {
        log.info("[exportTxt] enter, versionId={}", versionId);

        // 1. 校验版本
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        if (!DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            throw new RuntimeException("该版本不是文字版本，无法导出");
        }

        // 2. 查所有 line（按 sort 升序；sort 理论上非 null，兜底一下）
        List<DialogueLine> lines = dialogueLineMapper.listByVersionId(versionId);
        if (lines == null) lines = Collections.emptyList();
        lines.sort(Comparator.comparing(DialogueLine::getSort, Comparator.nullsLast(Integer::compareTo)));

        // 3. 查说话人名字：speakerId → role.name
        Map<Long, String> speakerNameMap = new HashMap<>();
        Set<Long> speakerIds = lines.stream()
                .map(DialogueLine::getSpeakerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!speakerIds.isEmpty()) {
            List<Role> roles = roleMapper.listByIds(speakerIds);
            if (roles != null) {
                for (Role r : roles) {
                    if (r.getId() != null) speakerNameMap.put(r.getId(), r.getName());
                }
            }
        }

        // 4. RC 场景：反查 rc_option，还原 dialogueRole + optionNumber
        Map<Long, Integer> lineIdToRole = new HashMap<>();
        Map<Long, Integer> lineIdToOptionNum = new HashMap<>();
        if (DialogueSourceTypeEnum.RC.getValue().equals(version.getSourceType())) {
            List<RcOption> options = rcOptionMapper.listByVersionIds(List.of(versionId));
            if (options != null) {
                for (RcOption opt : options) {
                    if (opt.getQuestionLineId() != null) {
                        lineIdToRole.put(opt.getQuestionLineId(), DialogueRoleEnum.QUESTION.getValue());
                        lineIdToOptionNum.put(opt.getQuestionLineId(), opt.getSort());
                    }
                    if (opt.getAnswerLineId() != null) {
                        lineIdToRole.put(opt.getAnswerLineId(), DialogueRoleEnum.ANSWER.getValue());
                        lineIdToOptionNum.put(opt.getAnswerLineId(), opt.getSort());
                    }
                }
            }
        }

        // 5. 组装 VO
        List<DialogueLineVO> lineVOs = new ArrayList<>(lines.size());
        for (DialogueLine line : lines) {
            DialogueLineVO vo = new DialogueLineVO();
            vo.setId(line.getId());
            vo.setSpeakerId(line.getSpeakerId());
            vo.setSpeakerName(speakerNameMap.get(line.getSpeakerId()));
            vo.setContent(line.getContent());
            vo.setSort(line.getSort());
            vo.setMonologue(line.getMonologue());
            vo.setDialogueRole(lineIdToRole.getOrDefault(line.getId(), DialogueRoleEnum.NORMAL.getValue()));
            vo.setOptionNumber(lineIdToOptionNum.get(line.getId()));
            lineVOs.add(vo);
        }

        // 6. 生成 txt 文本
        String text = buildTxtText(lineVOs);

        // 7. 写入响应（下载）
        String filename = "dialogue-" + versionId + ".txt";
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" +
                        URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(text);
            writer.flush();
            log.info("导出 txt 成功，versionId={}", versionId);
        } catch (Exception e) {
            log.error("导出 txt 失败，versionId={}", versionId, e);
            throw new RuntimeException("导出 txt 失败", e);
        }
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
     * 按「——分隔线」和「N. 选项编号」切块。
     * <p>
     * 规则：
     * <ul>
     *   <li>—— / ---：切块分隔符</li>
     *   <li>N. 开头（如 1. / 2. / 3.）：新选项块开始</li>
     *   <li>空行：普通块内切块（保留旁白识别）；选项块内不切（保持问句和回答连在一起）</li>
     * </ul>
     */
    private List<List<String>> splitIntoBlocks(String text) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        boolean inOptionBlock = false;

        String[] rawLines = text.split("\n");
        for (String raw : rawLines) {
            String line = raw.replace("\r", "").trim();

            // 1. 空行
            if (line.isEmpty()) {
                if (inOptionBlock) {
                    // 选项块内：空行不切块，问句和回答保持同一块
                    continue;
                }
                // 普通块：空行切块（旁白识别依赖此逻辑）
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                continue;
            }

            // 2. 分隔线 —— / ---
            if (line.equals("——") || line.matches("^-{3,}$")) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                inOptionBlock = false;
                continue;
            }

            // 3. N. 选项编号 → 新选项块开始
            if (OPTION_NUMBER_PATTERN.matcher(line).matches()) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
                inOptionBlock = true;
                current.add(line);
                continue;
            }

            // 4. 普通内容行
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
                    // ★ 只有 stage 0/1 才归到当前选项；stage >= 2 视为普通行
                    boolean actualOption = isOption && stage < 2;
                    DialogueRoleEnum role;
                    if (actualOption && stage == 0) {
                        role = DialogueRoleEnum.QUESTION;
                    } else if (actualOption && stage == 1) {
                        role = DialogueRoleEnum.ANSWER;
                    } else {
                        role = DialogueRoleEnum.NORMAL;
                    }
                    flushLine(lines, currentSpeaker, buffer.toString(), sort++,
                            role.getValue(), actualOption ? optionNumber : null,
                            roleCache, speakers, unmatchedSpeakers, unmatchedStickers, errors);
                    if (actualOption) {
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
            boolean actualOption = isOption && stage < 2;
            DialogueRoleEnum role;
            if (actualOption && stage == 0) {
                role = DialogueRoleEnum.QUESTION;
            } else if (actualOption && stage == 1) {
                role = DialogueRoleEnum.ANSWER;
            } else {
                role = DialogueRoleEnum.NORMAL;
            }
            flushLine(lines, currentSpeaker, buffer.toString(), sort++,
                    role.getValue(), actualOption ? optionNumber : null,
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

    /** 文本块类型 */
    private enum TxtBlockType { NORMAL, NARRATOR, OPTION }

    /** 逻辑块：一组连续的 line */
    private static class TxtBlock {
        TxtBlockType type;
        List<DialogueLineVO> lines;

        TxtBlock(TxtBlockType type, List<DialogueLineVO> lines) {
            this.type = type;
            this.lines = lines;
        }
    }

    /**
     * 把排好序的 lines 转换成 txt 文本。
     * <p>
     * 规则（与解析方法严格逆向）：
     * <ul>
     *   <li>普通块之间 → 空行</li>
     *   <li>涉及选项块时 → {@code ——} 分隔（选项块前后、选项之间）</li>
     *   <li>旁白块 → 直接输出 content（保留原括号）</li>
     *   <li>内心独白 → 内容自动补 {@code （...）}</li>
     *   <li>普通对话 → {@code 说话人：\n内容}</li>
     * </ul>
     */
    private String buildTxtText(List<DialogueLineVO> lines) {
        if (lines == null || lines.isEmpty()) return "";

        // 1. 分组
        List<TxtBlock> blocks = splitLinesToBlocks(lines);

        // 2. 拼装
        StringBuilder sb = new StringBuilder();
        TxtBlock lastBlock = null;

        for (TxtBlock block : blocks) {
            // 块间分隔
            if (lastBlock != null) {
                boolean lastIsOption = lastBlock.type == TxtBlockType.OPTION;
                boolean curIsOption = block.type == TxtBlockType.OPTION;
                if (lastIsOption || curIsOption) {
                    // ★ 选项块前是普通/旁白块 → 先补一个空行，再输出 ——
                    if (curIsOption && !lastIsOption) sb.append("\n");
                    sb.append("——\n");
                    // 选项块后接普通/旁白块，额外空行隔开
                    if (!curIsOption) sb.append("\n");
                } else {
                    sb.append("\n");
                }
            }

            // 输出块内容
            if (block.type == TxtBlockType.OPTION) {
                appendOptionBlock(sb, block.lines);
            } else if (block.type == TxtBlockType.NARRATOR) {
                for (DialogueLineVO line : block.lines) {
                    sb.append(nullToEmpty(line.getContent())).append("\n");
                }
            } else {
                for (DialogueLineVO line : block.lines) {
                    sb.append(formatLine(line)).append("\n");
                }
            }

            lastBlock = block;
        }

        // 最后一个块是选项时，末尾补一个 ——
        if (lastBlock != null && lastBlock.type == TxtBlockType.OPTION) {
            sb.append("——\n");
        }

        return sb.toString();
    }

    /**
     * 把 lines 分成逻辑块：
     * <ul>
     *   <li>连续的选项行 → 一个 OPTION 块</li>
     *   <li>连续的旁白行 → 一个 NARRATOR 块</li>
     *   <li>其余每行 → 一个 NORMAL 块</li>
     * </ul>
     */
    private List<TxtBlock> splitLinesToBlocks(List<DialogueLineVO> lines) {
        List<TxtBlock> blocks = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            DialogueLineVO line = lines.get(i);

            if (isOptionLine(line)) {
                List<DialogueLineVO> optLines = new ArrayList<>();
                while (i < lines.size() && isOptionLine(lines.get(i))) {
                    optLines.add(lines.get(i));
                    i++;
                }
                blocks.add(new TxtBlock(TxtBlockType.OPTION, optLines));
            } else if (isNarratorLine(line)) {
                List<DialogueLineVO> narratorLines = new ArrayList<>();
                while (i < lines.size() && isNarratorLine(lines.get(i))) {
                    narratorLines.add(lines.get(i));
                    i++;
                }
                blocks.add(new TxtBlock(TxtBlockType.NARRATOR, narratorLines));
            } else {
                blocks.add(new TxtBlock(TxtBlockType.NORMAL, List.of(line)));
                i++;
            }
        }
        return blocks;
    }

    /** 是否为选项行：有 optionNumber 且 role ∈ {QUESTION, ANSWER} */
    private boolean isOptionLine(DialogueLineVO line) {
        return line.getOptionNumber() != null
                && (DialogueRoleEnum.QUESTION.getValue().equals(line.getDialogueRole())
                || DialogueRoleEnum.ANSWER.getValue().equals(line.getDialogueRole()));
    }

    /** 是否为旁白行：speakerName 等于旁白角色名 */
    private boolean isNarratorLine(DialogueLineVO line) {
        return DialogueConstant.NARRATOR_ROLE_NAME.equals(line.getSpeakerName());
    }

    /**
     * 输出一个选项块组：按 optionNumber 分组，每组输出「问句 + 空行 + 回答」。
     * <p>
     * 输出示例：
     * <pre>{@code
     * ——
     * 1.
     * 纺：
     * 除了吉他，您还会其他乐器吗？
     *
     * 千：
     * 虽然不是每项都很擅长...
     * ——
     * 2.
     * ...
     * }</pre>
     */
    private void appendOptionBlock(StringBuilder sb, List<DialogueLineVO> optLines) {
        // 按 optionNumber 分组，保持出现顺序
        Map<Integer, List<DialogueLineVO>> grouped = new LinkedHashMap<>();
        for (DialogueLineVO line : optLines) {
            if (line.getOptionNumber() != null) {
                grouped.computeIfAbsent(line.getOptionNumber(), k -> new ArrayList<>()).add(line);
            }
        }

        boolean first = true;
        for (Map.Entry<Integer, List<DialogueLineVO>> entry : grouped.entrySet()) {
            if (!first) sb.append("——\n");
            first = false;

            sb.append(entry.getKey()).append(".\n");

            // 问句
            DialogueLineVO question = entry.getValue().stream()
                    .filter(l -> DialogueRoleEnum.QUESTION.getValue().equals(l.getDialogueRole()))
                    .findFirst().orElse(null);
            if (question != null) {
                sb.append(formatLine(question)).append("\n");
                sb.append("\n");  // 问句和回答之间空行
            }

            // 回答
            DialogueLineVO answer = entry.getValue().stream()
                    .filter(l -> DialogueRoleEnum.ANSWER.getValue().equals(l.getDialogueRole()))
                    .findFirst().orElse(null);
            if (answer != null) {
                sb.append(formatLine(answer)).append("\n");
            }
        }
    }

    /**
     * 格式化一行普通对话：{@code 说话人：\n内容}
     * <p>
     * 内心独白（monologue=1）时自动给内容加上 {@code （...）}。
     */
    private String formatLine(DialogueLineVO line) {
        String content = nullToEmpty(line.getContent());
        if (Integer.valueOf(1).equals(line.getMonologue())) {
            content = "（" + content + "）";
        }
        String speaker = nullToEmpty(line.getSpeakerName());
        return speaker + "：\n" + content;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
