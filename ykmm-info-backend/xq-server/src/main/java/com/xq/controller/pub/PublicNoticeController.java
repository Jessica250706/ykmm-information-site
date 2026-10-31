package com.xq.controller.pub;

import com.xq.dto.NoticePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.NoticeService;
import com.xq.vo.NoticeVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共 - 公告浏览
 * <p>
 * 游客可访问；已登录用户 / 管理员也会被 JwtTokenInterceptor 注入上下文，
 * 因此可见性过滤能在 Service 里准确区分三种身份。
 */
@RestController("PubNoticeController")
@RequestMapping("/pub/notice")
@Slf4j
@Validated
public class PublicNoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询公告（按当前访问者身份过滤可见性）
     */
    @GetMapping("/page")
    public Result<PageResult<NoticeVO>> page(
            @NotNull(message = "查询条件不能为空") NoticePageQueryDTO query) {
        return Result.success(noticeService.pageVisible(query));
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public Result<NoticeVO> detail(
            @PathVariable @NotNull(message = "公告ID不能为空") Long id) {
        return Result.success(noticeService.detail(id));
    }
}
