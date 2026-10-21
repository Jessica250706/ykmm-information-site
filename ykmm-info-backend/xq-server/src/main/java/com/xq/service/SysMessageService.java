package com.xq.service;

import com.xq.dto.SysMessagePageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.SysMessageVO;

/**
 * 系统消息服务
 */
public interface SysMessageService {

    /**
     * 分页查询消息
     * <p>
     * 权限：
     * <ul>
     *   <li>普通用户：只能查自己的消息</li>
     *   <li>管理员：可查全部，也可通过 query.userId 精确筛选某个用户</li>
     *   <li>游客：拒绝</li>
     * </ul>
     */
    PageResult<SysMessageVO> pageQuery(SysMessagePageQueryDTO query);

    /**
     * 查询消息详情
     * <p>
     * 普通用户只能查看自己的消息；管理员可查看任意。
     */
    SysMessageVO getById(Long id);

    /**
     * 统计当前登录用户的未读消息数
     */
    long countUnread();

    /**
     * 将指定消息标记为已读
     * <p>
     * 用户只能标记自己的；管理员可标记任意。
     */
    void markRead(Long id);

    /**
     * 将当前用户全部未读消息标记为已读
     */
    void markAllRead();

    /**
     * 发送消息（供内部业务调用，例如审核结果通知）
     *
     * @param userId     接收者用户ID
     * @param type       消息类型：1-审核通过 2-审核拒绝 3-系统通知
     * @param targetType 关联业务类型：1-卡面 2-对话版本 3-剧情
     * @param targetId   关联业务主键
     * @param title      标题
     * @param content    正文
     */
    void send(Long userId, Integer type, Integer targetType, Long targetId,
              String title, String content);
}
