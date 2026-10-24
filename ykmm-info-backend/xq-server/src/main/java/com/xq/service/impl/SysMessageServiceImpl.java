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
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }
        boolean isAdmin = RoleContext.isAdmin();

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<SysMessage> list = sysMessageMapper.pageQuery(query, currentId, isAdmin);
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

        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }

        // 广播消息：仅管理员能看
        if (msg.getBroadcastRole() != null) {
            if (!RoleContext.isAdmin()) {
                throw new ForbiddenException("无权查看该消息");
            }
        } else {
            // 个人消息：只能看自己的（管理员也可以看任意个人消息）
            if (!RoleContext.isAdmin() && !Objects.equals(msg.getUserId(), currentId)) {
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
        return sysMessageMapper.countUnread(currentId, RoleContext.isAdmin());
    }

    @Override
    @Transactional
    public void markRead(Long id) {
        if (id == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }

        SysMessage msg = sysMessageMapper.getById(id);
        if (msg == null) {
            throw new RuntimeException("消息不存在");
        }

        if (msg.getBroadcastRole() != null) {
            // 广播消息：仅管理员可操作；任一管理员操作即全群体已读
            if (!RoleContext.isAdmin()) {
                throw new ForbiddenException("无权操作该消息");
            }
            sysMessageMapper.markBroadcastRead(id, currentId);
        } else {
            // 个人消息：只有消息接收者本人可以标记
            if (!Objects.equals(msg.getUserId(), currentId)) {
                throw new ForbiddenException("无权操作该消息");
            }
            sysMessageMapper.markPersonalRead(id, currentId);
        }
    }

    @Override
    @Transactional
    public void markAllRead() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new UnauthorizedException("请先登录");
        }

        // 自己的个人消息全部已读
        sysMessageMapper.markAllPersonalRead(currentId);

        // 管理员额外把全部广播消息标记为已读
        if (RoleContext.isAdmin()) {
            sysMessageMapper.markAllBroadcastRead(currentId);
        }
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

    @Override
    @Transactional
    public void sendToRole(Integer broadcastRole, Integer type, Integer targetType,
                           Long targetId, String title, String content) {
        if (broadcastRole == null || title == null) {
            log.warn("广播消息参数不完整，跳过：broadcastRole={}, title={}", broadcastRole, title);
            return;
        }
        if (!SysMessageTypeEnum.isValid(type)) {
            log.warn("广播消息类型不合法，跳过：type={}", type);
            return;
        }
        if (targetType != null && !SysMessageTargetTypeEnum.isValid(targetType)) {
            log.warn("广播消息关联类型不合法，跳过：targetType={}", targetType);
            return;
        }

        SysMessage message = new SysMessage();
        message.setUserId(null);                 // 广播消息不绑定具体用户
        message.setBroadcastRole(broadcastRole);
        message.setType(type);
        message.setTargetType(targetType);
        message.setTargetId(targetId);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(SysMessageConstant.READ_STATUS_UNREAD);
        sysMessageMapper.insert(message);

        log.debug("广播消息已生成：role={}, type={}, targetType={}, targetId={}",
                broadcastRole, type, targetType, targetId);
    }

    /* ---------------- 私有方法 ---------------- */

    private SysMessageVO toVO(SysMessage msg) {
        if (msg == null) return null;
        SysMessageVO vo = new SysMessageVO();
        BeanUtils.copyProperties(msg, vo);
        vo.setTypeLabel(SysMessageTypeEnum.getLabel(msg.getType()));
        vo.setTargetTypeLabel(SysMessageTargetTypeEnum.getLabel(msg.getTargetType()));
        vo.setBroadcastRoleLabel(SysMessageConstant.broadcastRoleLabel(msg.getBroadcastRole()));
        return vo;
    }
}
