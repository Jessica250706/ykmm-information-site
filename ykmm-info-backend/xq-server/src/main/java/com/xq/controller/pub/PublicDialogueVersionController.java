package com.xq.controller.pub;

import com.xq.dto.DialogueSourceQueryDTO;
import com.xq.result.Result;
import com.xq.service.DialogueVersionService;
import com.xq.vo.DialogueVersionOptionVO;
import com.xq.vo.DialogueVersionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共 - 对话版本选项
 * <p>
 * 仅返回"已发布内容对应"的版本组合；具体对话内容由 PubCardController /
 * PubStoryController 拿详情时返回，并已经过滤掉未发布的行。
 */
@RestController("PubDialogueVersionController")
@RequestMapping("/pub/dialogue-version")
@Slf4j
@Validated
public class PublicDialogueVersionController {

    @Autowired
    private DialogueVersionService dialogueVersionService;

    /**
     * 查询全部文字版本选项
     */
    @GetMapping("/text-options")
    public Result<List<DialogueVersionOptionVO>> textOptions(@Valid DialogueSourceQueryDTO query) {
        return Result.success(dialogueVersionService.listTextVersionOptions(query.getSourceType(), query.getSourceId()));
    }

    /**
     * 查询全部图片版本选项
     */
    @GetMapping("/img-options")
    public Result<List<DialogueVersionOptionVO>> imgOptions(@Valid DialogueSourceQueryDTO query) {
        return Result.success(dialogueVersionService.listImageVersionOptions(query.getSourceType(), query.getSourceId()));
    }

    /**
     * 查询全部版本选项
     */
    @GetMapping("/options")
    public Result<List<DialogueVersionOptionVO>> options(@Valid DialogueSourceQueryDTO query) {
        return Result.success(dialogueVersionService.listVersionOptions(query.getSourceType(), query.getSourceId()));
    }

    /**
     * 查询版本详情
     *
     * @param id 版本ID
     * @return 版本详情
     */
    @GetMapping("/{id}")
    public Result<DialogueVersionVO> detail(@PathVariable @NotNull(message = "版本ID不能为空") Long id) {
        return Result.success(dialogueVersionService.detail(id));
    }
}
