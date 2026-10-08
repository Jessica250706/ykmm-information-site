package com.xq.controller.user;

import com.xq.dto.CardPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.CardService;
import com.xq.vo.CardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 卡面浏览
 */
@RestController("UserCardController")
@RequestMapping("/user/card")
@Slf4j
public class UserCardController {

    @Autowired
    private CardService cardService;

    /**
     * 分页查询已发布卡面
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<CardVO>> page(CardPageQueryDTO query) {
        return Result.success(cardService.pageQuery(query));
    }

    /**
     * 查询卡面详情（含对话）
     *
     * @param id 主键
     * @return 卡面详情
     */
    @GetMapping("/{id}")
    public Result<CardVO> detail(@PathVariable Long id) {
        return Result.success(cardService.getById(id));
    }
}
