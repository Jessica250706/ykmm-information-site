package com.xq.service.impl;

import com.xq.dto.DialogueImageDTO;
import com.xq.entity.DialogueImage;
import com.xq.entity.DialogueVersion;
import com.xq.enums.DialogueFormatEnum;
import com.xq.mapper.DialogueImageMapper;
import com.xq.mapper.DialogueVersionMapper;
import com.xq.service.DialogueImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话图片服务实现
 */
@Service
@Slf4j
public class DialogueImageServiceImpl implements DialogueImageService {

    @Autowired
    private DialogueImageMapper dialogueImageMapper;

    @Autowired
    private DialogueVersionMapper dialogueVersionMapper;

    /**
     * 保存图片列表
     *
     * @param versionId 版本ID
     * @param images    图片列表
     */
    @Override
    @Transactional
    public void saveImages(Long versionId, List<DialogueImageDTO> images) {
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }
        if (!DialogueFormatEnum.IMAGE.getValue().equals(version.getFormat())) {
            throw new RuntimeException("该版本不是图片版本");
        }

        // 先清空原有图片
        dialogueImageMapper.deleteByVersionId(versionId);

        List<DialogueImage> entityList = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            DialogueImageDTO dto = images.get(i);
            if (dto.getUrl() == null || dto.getUrl().isBlank()) {
                throw new RuntimeException("图片地址不能为空");
            }
            DialogueImage img = new DialogueImage();
            img.setVersionId(versionId);
            img.setUrl(dto.getUrl());
            img.setSort(i + 1);
            entityList.add(img);
        }
        dialogueImageMapper.insertBatch(entityList);
        log.info("保存对话图片成功，versionId={}, count={}", versionId, entityList.size());
    }

    /**
     * 删除单张图片
     *
     * @param imageId 图片ID
     */
    @Override
    @Transactional
    public void delete(Long imageId) {
        DialogueImage img = dialogueImageMapper.getById(imageId);
        if (img == null) {
            throw new RuntimeException("对话图片不存在");
        }
        dialogueImageMapper.deleteById(imageId);
        log.info("删除对话图片成功，id={}", imageId);
    }

    /**
     * 调整图片顺序
     *
     * @param versionId 版本ID
     * @param imageIds  图片ID顺序
     */
    @Override
    @Transactional
    public void sort(Long versionId, List<Long> imageIds) {
        DialogueVersion version = dialogueVersionMapper.getById(versionId);
        if (version == null) {
            throw new RuntimeException("对话版本不存在");
        }

        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            DialogueImage img = dialogueImageMapper.getById(imageId);
            if (img == null) {
                throw new RuntimeException("对话图片不存在：id=" + imageId);
            }
            if (!img.getVersionId().equals(versionId)) {
                throw new RuntimeException("图片不属于该版本：id=" + imageId);
            }
            dialogueImageMapper.updateSort(imageId, i + 1);
        }
        log.info("调整对话图片顺序成功，versionId={}", versionId);
    }
}
