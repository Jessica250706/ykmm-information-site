package com.xq.service;

import com.xq.dto.StoryPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.StoryCategoryVO;
import com.xq.vo.StoryDetailVO;
import com.xq.vo.StoryVO;

import java.util.List;

/**
 * 用户端 - 剧情浏览
 */
public interface UserStoryService {

    /**
     * 查询分类树（仅返回已发布状态相关节点）
     *
     * @param categoryType 分类类型
     * @return 分类树
     */
    List<StoryCategoryVO> categoryTree(Integer categoryType);

    /**
     * 分页查询分类下的剧情（只返回已发布）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<StoryVO> pageStory(StoryPageQueryDTO query);

    /**
     * 查询剧情详情，含所有对话版本
     *
     * @param id 剧情主键
     * @return 详情
     */
    StoryDetailVO storyDetail(Long id);
}
