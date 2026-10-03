package com.xq.controller.user;

import com.xq.dto.StoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.UserStoryService;
import com.xq.vo.StoryCategoryVO;
import com.xq.vo.StoryDetailVO;
import com.xq.vo.StoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 剧情浏览
 */
@RestController("UserStoryController")
@RequestMapping("/user/story")
@Slf4j
public class UserStoryController {

    @Autowired
    private UserStoryService userStoryService;

    /**
     * 查询剧情分类树
     *
     * @param categoryType 分类类型，null 返回全部
     * @return 分类树
     */
    @GetMapping("/category/tree")
    public Result<List<StoryCategoryVO>> categoryTree(
            @RequestParam(required = false) Integer categoryType) {
        return Result.success(userStoryService.categoryTree(categoryType));
    }

    /**
     * 分页查询某分类下的剧情
     *
     * @param query 查询条件（含 categoryId、pageNum、pageSize）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<StoryVO>> pageStory(StoryPageQueryDTO query) {
        return Result.success(userStoryService.pageStory(query));
    }

    /**
     * 查询剧情详情（含对话）
     *
     * @param id 剧情主键
     * @return 详情
     */
    @GetMapping("/{id}")
    public Result<StoryDetailVO> storyDetail(@PathVariable Long id) {
        return Result.success(userStoryService.storyDetail(id));
    }
}
