package com.xq.controller.pub;

import com.xq.dto.StoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.StoryService;
import com.xq.vo.StoryCategoryVO;
import com.xq.vo.StoryDetailVO;
import com.xq.vo.StoryVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共 - 剧情浏览
 */
@RestController("PubStoryController")
@RequestMapping("/pub/story")
@Slf4j
public class PublicStoryController {

    @Autowired
    private StoryService storyService;

    /**
     * 查询剧情分类树
     */
    @GetMapping("/category/tree")
    public Result<List<StoryCategoryVO>> categoryTree(@RequestParam(required = false) @Valid Integer categoryType) {
        return Result.success(storyService.categoryTree(categoryType));
    }

    /**
     * 分页查询剧情
     * <p>
     * - 游客：仅已发布
     * - 用户：已发布 + 自己创建的
     * - 管理员：全部
     */
    @GetMapping("/page")
    public Result<PageResult<StoryVO>> pageStory(StoryPageQueryDTO query) {
        return Result.success(storyService.pageQuery(query));
    }

    /**
     * 查询剧情详情（含对话版本，按身份过滤）
     */
    @GetMapping("/{id}")
    public Result<StoryDetailVO> storyDetail(@PathVariable Long id) {
        return Result.success(storyService.storyDetail(id));
    }
}
