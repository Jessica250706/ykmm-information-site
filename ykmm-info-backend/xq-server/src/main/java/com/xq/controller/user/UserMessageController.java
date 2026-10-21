package com.xq.controller.user;

import com.xq.dto.SysMessagePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.SysMessageService;
import com.xq.vo.SysMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 我的消息
 */
@RestController("UserMessageController")
@RequestMapping("/user/message")
@Slf4j
@Validated
public class UserMessageController {

    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 分页查询我的消息
     */
    @GetMapping("/page")
    public Result<PageResult<SysMessageVO>> page(@Validated SysMessagePageQueryDTO query) {
        return Result.success(sysMessageService.pageQuery(query));
    }

    /**
     * 查询消息详情
     */
    @GetMapping("/{id}")
    public Result<SysMessageVO> detail(@PathVariable Long id) {
        return Result.success(sysMessageService.getById(id));
    }

    /**
     * 我的未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(sysMessageService.countUnread());
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        sysMessageService.markRead(id);
        return Result.success();
    }

    /**
     * 标记全部消息为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        sysMessageService.markAllRead();
        return Result.success();
    }
}
