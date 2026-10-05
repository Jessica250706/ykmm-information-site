package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.dto.DialogueVersionDTO;
import com.xq.dto.DialogueVersionPageQueryDTO;
import com.xq.entity.DialogueImage;
import com.xq.entity.DialogueLine;
import com.xq.entity.DialogueSegment;
import com.xq.entity.DialogueVersion;
import com.xq.enums.DialogueFormatEnum;
import com.xq.enums.DialogueLanguageEnum;
import com.xq.enums.DialogueScopeEnum;
import com.xq.enums.DialogueSourceTypeEnum;
import com.xq.mapper.DialogueImageMapper;
import com.xq.mapper.DialogueLineMapper;
import com.xq.mapper.DialogueSegmentMapper;
import com.xq.mapper.DialogueVersionMapper;
import com.xq.result.PageResult;
import com.xq.service.DialogueVersionService;
import com.xq.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 分页查询对话版本
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<DialogueVersionVO> pageQuery(DialogueVersionPageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<DialogueVersion> page = (Page<DialogueVersion>)
                dialogueVersionMapper.pageQuery(query);
        List<DialogueVersionVO> voList = page.getResult().stream()
                .map(this::toBasicVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), voList);
    }

    /**
     * 查询某来源下的全部版本
     *
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     * @return 版本列表
     */
    @Override
    public List<DialogueVersionVO> listBySource(Integer sourceType, Long sourceId) {
        if (!DialogueSourceTypeEnum.isValid(sourceType)) {
            throw new RuntimeException("来源类型不合法");
        }
        if (sourceId == null) {
            throw new RuntimeException("来源ID不能为空");
        }
        List<DialogueVersion> list =
                dialogueVersionMapper.listBySource(sourceType, sourceId);
        List<DialogueVersionVO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (DialogueVersion v : list) {
            result.add(toBasicVO(v));
        }
        return result;
    }

    /**
     * 查询版本详情
     *
     * @param versionId 版本ID
     * @return 版本详情
     */
    @Override
    public DialogueVersionVO detail(Long versionId) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        DialogueVersionVO vo = toBasicVO(version);

        // 根据 format 加载内容
        if (DialogueFormatEnum.TEXT.getValue().equals(version.getFormat())) {
            vo.setLines(loadLines(versionId));
        } else if (DialogueFormatEnum.IMAGE.getValue().equals(version.getFormat())) {
            vo.setImages(loadImages(versionId));
        }
        return vo;
    }

    /**
     * 创建版本
     *
     * @param dto 创建参数
     * @return 新版本ID
     */
    @Override
    @Transactional
    public Long create(DialogueVersionDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (!DialogueSourceTypeEnum.isValid(dto.getSourceType())) {
            throw new RuntimeException("来源类型不合法");
        }
        if (dto.getSourceId() == null) {
            throw new RuntimeException("来源ID不能为空");
        }
        if (!DialogueLanguageEnum.isValid(dto.getLanguage())) {
            throw new RuntimeException("语言不合法");
        }
        if (!DialogueFormatEnum.isValid(dto.getFormat())) {
            throw new RuntimeException("形式不合法");
        }
        if (!DialogueScopeEnum.isValid(dto.getScope())) {
            throw new RuntimeException("范围不合法");
        }

        // 唯一性校验
        DialogueVersion exist = dialogueVersionMapper.getByUniqueKey(
                dto.getSourceType(), dto.getSourceId(),
                dto.getLanguage(), dto.getFormat(), dto.getScope());
        if (exist != null) {
            throw new RuntimeException("该组合的对话版本已存在");
        }

        DialogueVersion version = new DialogueVersion();
        BeanUtils.copyProperties(dto, version);
        dialogueVersionMapper.insert(version);
        log.info("创建对话版本成功，id={}", version.getId());
        return version.getId();
    }

    /**
     * 删除版本
     *
     * @param versionId 版本ID
     */
    @Override
    @Transactional
    public void delete(Long versionId) {
        if (versionId == null) {
            throw new RuntimeException("版本ID不能为空");
        }
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }

        // 级联删除文字内容
        List<DialogueLine> lines =
                dialogueLineMapper.listByVersionId(versionId);
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
        log.info("删除对话版本成功，id={}", versionId);
    }

    /**
     * 查询全部文字版本选项
     *
     * @param sourceType 来源类型
     * @param sourceId   来源主键
     * @return 选项列表
     */
    @Override
    public List<DialogueVersionOptionVO> listTextVersionOptions(Integer sourceType, Long sourceId) {
        if (!DialogueSourceTypeEnum.isValid(sourceType)) {
            throw new RuntimeException("来源类型不合法");
        }
        if (sourceId == null) {
            throw new RuntimeException("来源ID不能为空");
        }

        // 1. 查出当前来源下已有的文字版本
        List<DialogueVersion> existed =
                dialogueVersionMapper.listBySourceAndFormat(
                        sourceType, sourceId, DialogueFormatEnum.TEXT.getValue());

        // 2. 建索引：language-scope -> versionId
        Map<String, Long> existedMap = new HashMap<>();
        if (existed != null) {
            for (DialogueVersion v : existed) {
                existedMap.put(keyOf(v.getLanguage(), v.getScope()), v.getId());
            }
        }

        // 3. 枚举所有语言 × 范围的组合
        List<DialogueVersionOptionVO> result = new ArrayList<>();
        for (DialogueLanguageEnum lang : DialogueLanguageEnum.values()) {
            for (DialogueScopeEnum scope : DialogueScopeEnum.values()) {
                DialogueVersionOptionVO vo = new DialogueVersionOptionVO();
                vo.setLanguage(lang.getValue());
                vo.setLanguageLabel(lang.getLabel());
                vo.setFormat(DialogueFormatEnum.TEXT.getValue());
                vo.setFormatLabel(DialogueFormatEnum.TEXT.getLabel());
                vo.setScope(scope.getValue());
                vo.setScopeLabel(scope.getLabel());
                vo.setLabel(lang.getLabel() + " · " + DialogueFormatEnum.TEXT.getLabel()
                        + " · " + scope.getLabel());

                Long versionId = existedMap.get(keyOf(lang.getValue(), scope.getValue()));
                vo.setVersionId(versionId);
                vo.setExists(versionId != null);

                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 查询全部文字版本选项
     *
     * @param sourceType 来源类型
     * @param sourceId   来源主键
     * @return 选项列表
     */
    @Override
    public List<DialogueVersionOptionVO> listVersionOptions(Integer sourceType, Long sourceId) {
        if (!DialogueSourceTypeEnum.isValid(sourceType)) {
            throw new RuntimeException("来源类型不合法");
        }
        if (sourceId == null) {
            throw new RuntimeException("来源ID不能为空");
        }

        // 1. 查出当前来源下已有的文字版本
        List<DialogueVersion> existed = dialogueVersionMapper.listBySource(sourceType, sourceId);

        // 2. 建索引：language-scope -> versionId
        Map<String, Long> existedMap = new HashMap<>();
        if (existed != null) {
            for (DialogueVersion v : existed) {
                existedMap.put(keyOf(v.getLanguage(), v.getFormat(), v.getScope()), v.getId());
            }
        }

        // 3. 枚举所有语言 × 范围的组合
        List<DialogueVersionOptionVO> result = new ArrayList<>();
        for (DialogueLanguageEnum lang : DialogueLanguageEnum.values()) {
            for (DialogueScopeEnum scope : DialogueScopeEnum.values()) {
                for (DialogueFormatEnum format : DialogueFormatEnum.values()) {
                    DialogueVersionOptionVO vo = new DialogueVersionOptionVO();
                    vo.setLanguage(lang.getValue());
                    vo.setLanguageLabel(lang.getLabel());
                    vo.setFormat(format.getValue());
                    vo.setFormatLabel(format.getLabel());
                    vo.setScope(scope.getValue());
                    vo.setScopeLabel(scope.getLabel());
                    vo.setLabel(lang.getLabel() + " · " + format.getLabel() + " · " + scope.getLabel());

                    Long versionId = existedMap.get(keyOf(lang.getValue(), format.getValue(), scope.getValue()));
                    vo.setVersionId(versionId);
                    vo.setExists(versionId != null);

                    result.add(vo);
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------
    // 私有方法
    // ---------------------------------------------------

    /**
     * 基础 VO 转换
     *
     * @param version 实体
     * @return VO
     */
    private DialogueVersionVO toBasicVO(DialogueVersion version) {
        if (version == null) {
            return null;
        }
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
     *
     * @param versionId 版本ID
     * @return 句子列表
     */
    private List<DialogueLineVO> loadLines(Long versionId) {
        List<DialogueLine> lines = dialogueLineMapper.listByVersionId(versionId);
        List<DialogueLineVO> result = new ArrayList<>();
        if (lines == null || lines.isEmpty()) {
            return result;
        }

        List<Long> lineIds = lines.stream()
                .map(DialogueLine::getId)
                .collect(Collectors.toList());
        List<DialogueSegment> allSegments =
                dialogueSegmentMapper.listByLineIds(lineIds);

        Map<Long, List<DialogueSegment>> segmentMap = new HashMap<>();
        if (allSegments != null) {
            for (DialogueSegment s : allSegments) {
                segmentMap.computeIfAbsent(s.getLineId(), k -> new ArrayList<>()).add(s);
            }
        }

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
     *
     * @param versionId 版本ID
     * @return 图片列表
     */
    private List<DialogueImageVO> loadImages(Long versionId) {
        List<DialogueImage> images = dialogueImageMapper.listByVersionId(versionId);
        List<DialogueImageVO> result = new ArrayList<>();
        if (images == null) {
            return result;
        }
        for (DialogueImage img : images) {
            DialogueImageVO vo = new DialogueImageVO();
            BeanUtils.copyProperties(img, vo);
            result.add(vo);
        }
        return result;
    }

    /**
     * 生成 language-scope 键
     *
     * @param language 语言
     * @param scope    范围
     * @return 键
     */
    private String keyOf(Integer language, Integer scope) {
        return language + "-" + scope;
    }

    /**
     * 组合 key：language-format-scope
     * 用于把「枚举组合」映射到「已存在的 versionId」
     */
    private String keyOf(Integer language, Integer format, Integer scope) {
        return language + "-" + format + "-" + scope;
    }
}
