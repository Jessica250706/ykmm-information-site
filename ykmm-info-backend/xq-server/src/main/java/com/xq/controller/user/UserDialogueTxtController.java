package com.xq.controller.user;

import com.xq.dto.DialogueTxtImportDTO;
import com.xq.result.Result;
import com.xq.service.DialogueTxtService;
import com.xq.vo.DialogueTxtParseVO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端 - txt 解析导入
 */
@RestController("UserDialogueTxtController")
@RequestMapping("/user/dialogue-txt")
@Slf4j
@Validated
public class UserDialogueTxtController {

    @Autowired
    private DialogueTxtService dialogueTxtService;

    /**
     * 解析 txt，返回预览
     */
    @PostMapping("/parse/{versionId}")
    public Result<DialogueTxtParseVO> parseTxt(
            @PathVariable @NotNull(message = "版本ID不能为空") Long versionId,
            @RequestParam("file") @NotEmpty(message = "文件不能为空") MultipartFile file
    ) {
        return Result.success(dialogueTxtService.parseTxt(versionId, file));
    }

    /**
     * 确认导入
     */
    @PostMapping("/import/{versionId}")
    public Result<Void> importParsed(@PathVariable @NotNull(message = "版本ID不能为空") Long versionId,
                                     @RequestBody @NotEmpty(message = "导入内容不能为空") DialogueTxtImportDTO dto) {
        dialogueTxtService.importParsed(versionId, dto);
        return Result.success();
    }
}
