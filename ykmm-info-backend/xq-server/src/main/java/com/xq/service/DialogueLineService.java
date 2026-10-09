package com.xq.service;

import com.xq.dto.DialogueLineDTO;

import java.util.List;

/**
 * 对话句子服务
 */
public interface DialogueLineService {

    /**
     * 批量保存句子
     *
     * @param versionId 版本ID
     * @param lines     句子列表
     */
    void saveBatch(Long versionId, List<DialogueLineDTO> lines);

    /**
     * 编辑单句
     *
     * @param lineId 句子ID
     * @param dto    编辑参数
     */
    void update(Long lineId, DialogueLineDTO dto);

    /**
     * 删除单句
     *
     * @param lineId 句子ID
     */
    void delete(Long lineId);

    /**
     * 调整句子顺序
     *
     * @param versionId 版本ID
     * @param lineIds   句子ID顺序
     */
    void sort(Long versionId, List<Long> lineIds);

    /**
     * 清空指定版本下所有对话句子及其关联数据
     * 用于 txt 导入前的覆盖操作
     *
     * @param versionId 版本ID
     */
    void clearByVersionId(Long versionId);
}
