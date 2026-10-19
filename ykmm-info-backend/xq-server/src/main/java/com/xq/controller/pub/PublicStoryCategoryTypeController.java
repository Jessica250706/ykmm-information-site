package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.StoryCategoryTypeService;
import com.xq.vo.StoryCategoryTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共 - 剧情分类类型管理
 */
@RestController("PublicStoryCategoryTypeController")
@RequestMapping("/pub/story-category-type")
@Slf4j
public class PublicStoryCategoryTypeController {

    @Autowired
    private StoryCategoryTypeService storyCategoryTypeService;

    /**
     * 查询全部
     *
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<StoryCategoryTypeVO>> list() {
        return Result.success(storyCategoryTypeService.listAll());
    }
}
