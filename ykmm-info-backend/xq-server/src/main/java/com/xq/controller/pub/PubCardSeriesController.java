package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CardSeriesService;
import com.xq.vo.CardSeriesVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡面系列管理
 */
@RestController("PubCardSeriesController")
@RequestMapping("/pub/card-series")
@Slf4j
public class PubCardSeriesController {

    @Autowired
    private CardSeriesService cardSeriesService;

    /**
     * 查询全部（下拉用）
     *
     * @return 系列列表
     */
    @GetMapping("/options")
    public Result<List<CardSeriesVO>> listAll() {
        return Result.success(cardSeriesService.listAll());
    }

    /**
     * 按名称搜索（下拉远程搜索用）
     *
     * @param keyword 关键字
     * @return 系列列表
     */
    @GetMapping("/search")
    public Result<List<CardSeriesVO>> search(@RequestParam(required = false) String keyword) {
        return Result.success(cardSeriesService.searchByName(keyword));
    }
}
