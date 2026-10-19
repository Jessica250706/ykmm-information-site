package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.CommonService;
import com.xq.vo.DictItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共 - 字典
 */
@RestController("PubDictController")
@RequestMapping("/pub/dict")
@Slf4j
public class PublicDictController {

    @Autowired
    private CommonService commonService;

    /**
     * 卡面类别字典
     */
    @GetMapping("/card-category")
    public Result<List<DictItemVO>> getCardCategoryDict() {
        return Result.success(commonService.getCardCategoryDict());
    }

    /**
     * 属性字典（Shout/Beat/Melody）
     */
    @GetMapping("/attributes")
    public Result<List<DictItemVO>> getAttributeDict() {
        return Result.success(commonService.getAttributeDict());
    }

    /**
     * 稀有度字典（SSR/UR）
     */
    @GetMapping("/rarity")
    public Result<List<DictItemVO>> getRarityDict() {
        return Result.success(commonService.getRarityDict());
    }

    /**
     * 剧情分类类型字典
     */
    @GetMapping("/story-category-type")
    public Result<List<DictItemVO>> getStoryCategoryTypeDict() {
        return Result.success(commonService.getStoryCategoryTypeDict());
    }
}
