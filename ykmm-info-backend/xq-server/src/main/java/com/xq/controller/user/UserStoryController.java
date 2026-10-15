package com.xq.controller.user;

import com.xq.dto.StoryDTO;
import com.xq.result.Result;
import com.xq.service.StoryService;
import com.xq.validation.CreateGroup;
import com.xq.validation.UpdateGroup;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 剧情（写操作，仅自己）
 */
@RestController("UserStoryController")
@RequestMapping("/user/story")
@Slf4j
@Validated
public class UserStoryController {

    @Autowired
    private StoryService storyService;

    /**
     * 新增剧情
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated(CreateGroup.class) StoryDTO dto) {
        return Result.success(storyService.create(dto));
    }

    /**
     * 编辑剧情
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "剧情ID不能为空") Long id,
            @RequestBody @Validated(UpdateGroup.class) StoryDTO dto) {
        storyService.update(id, dto);
        return Result.success();
    }
}
