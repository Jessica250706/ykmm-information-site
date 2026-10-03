package com.xq.controller.admin;

import com.xq.dto.DialogueTxtImportDTO;
import com.xq.result.Result;
import com.xq.service.DialogueTxtService;
import com.xq.vo.DialogueTxtParseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * txt 解析导入
 */
@RestController("AdminDialogueTxtController")
@RequestMapping("/admin/dialogue-txt")
@Slf4j
public class DialogueTxtController {

    @Autowired
    private DialogueTxtService dialogueTxtService;

    /**
     * 解析 txt，返回预览
     *
     * @param versionId 版本ID
     * @param file      txt 文件
     * @return 解析结果
     */
    @PostMapping("/parse/{versionId}")
    public Result<DialogueTxtParseVO> parseTxt(@PathVariable Long versionId,
                                               @RequestParam("file") MultipartFile file) {
        return Result.success(dialogueTxtService.parseTxt(versionId, file));
    }

    /**
     * 确认导入解析结果
     *
     * @param versionId 版本ID
     * @param dto       确认后的句子
     * @return 统一返回
     */
    @PostMapping("/import/{versionId}")
    public Result<Void> importParsed(@PathVariable Long versionId,
                                     @RequestBody DialogueTxtImportDTO dto) {
        dialogueTxtService.importParsed(versionId, dto);
        return Result.success();
    }
}