package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
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
 * 管理端 - 消息管理
 */
@RestController("AdminMessageController")
@RequestMapping("/admin/message")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
public class MessageController {

    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 分页查询消息
     * <p>
     * 管理员可查全部，也可通过 query.userId 精确筛选某个用户。
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
}
