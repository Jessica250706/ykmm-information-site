package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.NoticeCategoryService;
import com.xq.vo.NoticeCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共 - 公告分类列表（游客可访问，用于前端筛选）
 */
@RestController("PubNoticeCategoryController")
@RequestMapping("/pub/notice-category")
@Slf4j
@Validated
public class PublicNoticeCategoryController {

    @Autowired
    private NoticeCategoryService noticeCategoryService;

    @GetMapping("/list")
    public Result<List<NoticeCategoryVO>> list() {
        return Result.success(noticeCategoryService.listAll());
    }
}
