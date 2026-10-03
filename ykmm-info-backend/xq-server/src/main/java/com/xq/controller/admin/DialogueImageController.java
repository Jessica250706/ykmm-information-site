package com.xq.controller.admin;

import com.xq.dto.DialogueImageDTO;
import com.xq.result.Result;
import com.xq.service.DialogueImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对话图片管理
 */
@RestController("AdminDialogueImageController")
@RequestMapping("/admin/dialogue-images")
@Slf4j
public class DialogueImageController {

    @Autowired
    private DialogueImageService dialogueImageService;

    /**
     * 保存图片列表
     *
     * @param versionId 版本ID
     * @param images    图片列表
     * @return 统一返回
     */
    @PostMapping("/batch/{versionId}")
    public Result<Void> saveImages(@PathVariable Long versionId,
                                   @RequestBody List<DialogueImageDTO> images) {
        dialogueImageService.saveImages(versionId, images);
        return Result.success();
    }

    /**
     * 删除单张图片
     *
     * @param id 图片ID
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dialogueImageService.delete(id);
        return Result.success();
    }

    /**
     * 调整图片顺序
     *
     * @param versionId 版本ID
     * @param imageIds  图片ID顺序
     * @return 统一返回
     */
    @PutMapping("/sort/{versionId}")
    public Result<Void> sort(@PathVariable Long versionId,
                             @RequestBody List<Long> imageIds) {
        dialogueImageService.sort(versionId, imageIds);
        return Result.success();
    }
}
