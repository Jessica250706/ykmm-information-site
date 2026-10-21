package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.SysMessageConstant;
import com.xq.context.BaseContext;
import com.xq.context.RoleContext;
import com.xq.dto.SysMessagePageQueryDTO;
import com.xq.entity.SysMessage;
import com.xq.enums.SysMessageTargetTypeEnum;
import com.xq.enums.SysMessageTypeEnum;
import com.xq.exception.ForbiddenException;
import com.xq.exception.UnauthorizedException;
import com.xq.mapper.SysMessageMapper;
import com.xq.result.PageResult;
import com.xq.service.SysMessageService;
import com.xq.vo.SysMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统消息服务实现
 */
@Service
@Slf4j
public class SysMessageServiceImpl implements SysMessageService {

    @Autowired
    private SysMessageMapper sysMessageMapper;

    @Override
    public PageResult<SysMessageVO> pageQuery(SysMessagePageQueryDTO query) {
        Long viewerId = resolveViewerId();

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<SysMessage> list = sysMessageMapper.pageQuery(query, viewerId);
        PageInfo<SysMessage> page = new PageInfo<>(list);

        List<SysMessageVO> voList = page.getList().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), voList);
    }

    @Override
    public SysMessageVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        SysMessage msg = sysMessageMapper.getById(id);
        if (msg == null) {
            throw new RuntimeException("消息不存在");
        }

        // 权限：普通用户只能看自己的消息
        if (!RoleContext.isAdmin()) {
            Long currentId = BaseContext.getCurrentId();
            if (currentId == null) {
                throw new UnauthorizedException("请先登录");
            }
            if (!Objects.equals(msg.getUserId(), currentId)) {
                throw new ForbiddenException("无权查看该消息");
            }
        }
        return toVO(msg);
    }

    @Override
    public long countUnread() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }
        return sysMessageMapper.countUnread(currentId);
    }

    @Override
    @Transactional
    public void markRead(Long id) {
        if (id == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        // 管理员：不限制 user_id；普通用户：强制限定为当前用户
        Long userId = RoleContext.isAdmin() ? null : BaseContext.getCurrentId();
        if (!RoleContext.isAdmin() && userId == null) {
            throw new UnauthorizedException("请先登录");
        }
        sysMessageMapper.markRead(id, userId);
    }

    @Override
    @Transactional
    public void markAllRead() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }
        sysMessageMapper.markAllRead(currentId);
    }

    @Override
    @Transactional
    public void send(Long userId, Integer type, Integer targetType, Long targetId,
                     String title, String content) {
        if (userId == null || title == null) {
            log.warn("消息参数不完整，跳过插入：userId={}, title={}", userId, title);
            return;
        }
        if (!SysMessageTypeEnum.isValid(type)) {
            log.warn("消息类型不合法，跳过插入：type={}", type);
            return;
        }
        if (targetType != null && !SysMessageTargetTypeEnum.isValid(targetType)) {
            log.warn("关联业务类型不合法，跳过插入：targetType={}", targetType);
            return;
        }

        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setType(type);
        message.setTargetType(targetType);
        message.setTargetId(targetId);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(SysMessageConstant.READ_STATUS_UNREAD);
        sysMessageMapper.insert(message);

        log.debug("系统消息已生成：userId={}, type={}, targetType={}, targetId={}",
                userId, type, targetType, targetId);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 解析"数据可见性"的用户ID：
     * <ul>
     *   <li>游客：未登录 → 拒绝</li>
     *   <li>普通用户：返回当前用户ID（SQL 里只看自己）</li>
     *   <li>管理员：返回 null（SQL 不加 user_id 限制）</li>
     * </ul>
     */
    private Long resolveViewerId() {
        if (RoleContext.isAdmin()) {
            return null;
        }
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }
        return currentId;
    }

    private SysMessageVO toVO(SysMessage msg) {
        if (msg == null) return null;
        SysMessageVO vo = new SysMessageVO();
        BeanUtils.copyProperties(msg, vo);
        vo.setTypeLabel(SysMessageTypeEnum.getLabel(msg.getType()));
        vo.setTargetTypeLabel(SysMessageTargetTypeEnum.getLabel(msg.getTargetType()));
        return vo;
    }
}
