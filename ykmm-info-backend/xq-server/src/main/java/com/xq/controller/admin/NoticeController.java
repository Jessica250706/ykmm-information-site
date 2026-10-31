package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.dto.NoticeDTO;
import com.xq.dto.NoticePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.result.Result;
import com.xq.service.NoticeService;
import com.xq.vo.NoticeVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 公告管理
 */
@RestController("AdminNoticeController")
@RequestMapping("/admin/notice")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
@Validated
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询（全部公告，含草稿和已下线）
     */
    @GetMapping("/page")
    public Result<PageResult<NoticeVO>> page(
            @NotNull(message = "查询条件不能为空") NoticePageQueryDTO query) {
        return Result.success(noticeService.pageAdmin(query));
    }

    /**
     * 详情（不做可见性过滤）
     */
    @GetMapping("/{id}")
    public Result<NoticeVO> detail(
            @PathVariable @NotNull(message = "公告ID不能为空") Long id) {
        return Result.success(noticeService.adminDetail(id));
    }

    /**
     * 新增（默认落为草稿）
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated NoticeDTO dto) {
        return Result.success(noticeService.create(dto));
    }

    /**
     * 编辑
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "公告ID不能为空") Long id,
            @RequestBody @Validated NoticeDTO dto) {
        noticeService.update(id, dto);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable @NotNull(message = "公告ID不能为空") Long id) {
        noticeService.delete(id);
        return Result.success();
    }

    /**
     * 发布
     */
    @PutMapping("/{id}/publish")
    public Result<Void> publish(
            @PathVariable @NotNull(message = "公告ID不能为空") Long id) {
        noticeService.publish(id);
        return Result.success();
    }

    /**
     * 下线
     */
    @PutMapping("/{id}/offline")
    public Result<Void> offline(
            @PathVariable @NotNull(message = "公告ID不能为空") Long id) {
        noticeService.offline(id);
        return Result.success();
    }
}
