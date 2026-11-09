package com.xq.service;

import com.xq.dto.DialogueTxtImportDTO;
import com.xq.vo.DialogueTxtParseVO;
import jakarta.servlet.http.HttpServletResponse;
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

    /**
     * 导出为 txt 文本（按解析逻辑逆向）
     * <p>
     * 直接将 txt 内容写入响应流，触发浏览器下载。
     *
     * @param versionId 版本ID（必须是文字版本）
     * @param response  HTTP 响应
     */
    void exportTxt(Long versionId, HttpServletResponse response);
}
