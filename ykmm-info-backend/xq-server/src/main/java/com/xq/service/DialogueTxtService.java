package com.xq.service;

import com.xq.dto.DialogueTxtImportDTO;
import com.xq.vo.DialogueTxtParseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * txt 解析导入服务
 */
public interface DialogueTxtService {

    /**
     * 解析 txt，返回预览
     *
     * @param versionId 版本ID
     * @param file      txt 文件
     * @return 解析结果
     */
    DialogueTxtParseVO parseTxt(Long versionId, MultipartFile file);

    /**
     * 确认导入解析结果
     *
     * @param versionId 版本ID
     * @param dto       确认后的句子
     */
    void importParsed(Long versionId, DialogueTxtImportDTO dto);
}
