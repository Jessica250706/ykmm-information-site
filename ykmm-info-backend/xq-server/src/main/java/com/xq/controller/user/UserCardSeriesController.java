package com.xq.controller.user;

import com.xq.result.Result;
import com.xq.service.CardSeriesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面系列管理
 */
@RestController("UserCardSeriesController")
@RequestMapping("/user/card-series")
@Slf4j
public class UserCardSeriesController {

    @Autowired
    private CardSeriesService cardSeriesService;

    /**
     * 按名称查询或创建（下拉"输入不存在则新增"场景）
     *
     * @param name 系列名
     * @return 系列ID
     */
    @PostMapping("/find-or-create")
    public Result<Long> findOrCreate(@RequestParam String name) {
        return Result.success(cardSeriesService.findOrCreateByName(name));
    }
}
