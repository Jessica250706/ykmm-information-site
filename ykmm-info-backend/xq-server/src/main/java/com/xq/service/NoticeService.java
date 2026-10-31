package com.xq.service;

import com.xq.dto.NoticeDTO;
import com.xq.dto.NoticePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.NoticeVO;

/**
 * 公告服务
 */
public interface NoticeService {

    /**
     * 公共端分页（游客 + 用户 + 管理员均可调）：
     * 按当前访问者级别过滤可见性，只返回已发布 + 未过期 + 到时间的公告
     */
    PageResult<NoticeVO> pageVisible(NoticePageQueryDTO query);

    /**
     * 公共端详情：
     * 非管理员需通过状态 + 可见性 + 时间三重校验；详情访问会累计浏览量
     */
    NoticeVO detail(Long id);

    /**
     * 管理端分页（全部公告）
     */
    PageResult<NoticeVO> pageAdmin(NoticePageQueryDTO query);

    /**
     * 管理端详情（不做可见性过滤）
     */
    NoticeVO adminDetail(Long id);

    /**
     * 新增（默认落为草稿）
     */
    Long create(NoticeDTO dto);

    /**
     * 编辑（不改变状态，状态变更走 publish/offline）
     */
    void update(Long id, NoticeDTO dto);

    /**
     * 删除
     */
    void delete(Long id);

    /**
     * 发布（草稿/已下线 → 已发布；publishAt 为空时置为当前时间）
     */
    void publish(Long id);

    /**
     * 下线（已发布 → 已下线）
     */
    void offline(Long id);
}
