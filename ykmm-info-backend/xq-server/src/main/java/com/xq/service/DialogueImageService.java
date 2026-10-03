package com.xq.service;

import com.xq.dto.DialogueImageDTO;

import java.util.List;

/**
 * 对话图片服务
 */
public interface DialogueImageService {

    /**
     * 保存图片列表
     *
     * @param versionId 版本ID
     * @param images    图片列表
     */
    void saveImages(Long versionId, List<DialogueImageDTO> images);

    /**
     * 删除单张图片
     *
     * @param imageId 图片ID
     */
    void delete(Long imageId);

    /**
     * 调整图片顺序
     *
     * @param versionId 版本ID
     * @param imageIds  图片ID顺序
     */
    void sort(Long versionId, List<Long> imageIds);
}
