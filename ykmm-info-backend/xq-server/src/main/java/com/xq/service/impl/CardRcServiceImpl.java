package com.xq.service.impl;

import com.xq.context.BaseContext;
import com.xq.dto.CardRcDTO;
import com.xq.entity.CardRc;
import com.xq.enums.DialogueSideEnum;
import com.xq.exception.ForbiddenException;
import com.xq.mapper.CardRcMapper;
import com.xq.service.CardRcService;
import com.xq.vo.CardRcVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 卡面 RC 服务实现
 */
@Service
@Slf4j
public class CardRcServiceImpl implements CardRcService {

    @Autowired
    private CardRcMapper cardRcMapper;

    /**
     * 查询卡面下的 RC 列表
     */
    @Override
    public List<CardRcVO> listByCardId(Long cardId) {
        List<CardRcVO> voList = cardRcMapper.listVOByCardId(cardId);

        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();

        for (CardRcVO rc : voList) {
            Boolean can = cardRcMapper.canOperate(rc.getId(), userId, role);
            rc.setCanOperate(Boolean.TRUE.equals(can));
        }
        return voList;
    }

    /**
     * 查询详情
     */
    @Override
    public CardRcVO getById(Long id) {
        CardRcVO vo = cardRcMapper.getVOById(id);
        if (vo == null) {
            throw new RuntimeException("RC 不存在");
        }
        return vo;
    }

    /**
     * 新增
     */
    @Override
    @Transactional
    public Long create(CardRcDTO dto) {
        CardRc rc = new CardRc();
        BeanUtils.copyProperties(dto, rc);
        rc.setCreatorId(BaseContext.getCurrentId());
        cardRcMapper.insert(rc);
        log.info("新增卡面 RC 成功，id={}", rc.getId());
        return rc.getId();
    }

    /**
     * 编辑
     */
    @Override
    @Transactional
    public void update(Long id, CardRcDTO dto) {
        checkOperable(id);
        CardRc rc = new CardRc();
        BeanUtils.copyProperties(dto, rc);
        rc.setId(id);
        cardRcMapper.update(rc);
        log.info("编辑卡面 RC 成功，id={}", id);
    }

    /**
     * 删除
     */
    @Override
    @Transactional
    public void delete(Long id) {
        checkOperable(id);
        cardRcMapper.deleteById(id);
        log.info("删除卡面 RC 成功，id={}", id);
    }

    /**
     * 计算某句话应该落库的 side 值
     *
     * @param rcId      卡面 RC 主键
     * @param speakerId 说话角色ID
     * @return side 值：发起人 → 2（右侧），其他角色 → 1（左侧）
     */
    @Override
    public Integer resolveSide(Long rcId, Long speakerId) {
        if (rcId == null) {
            throw new RuntimeException("RC ID不能为空");
        }
        if (speakerId == null) {
            throw new RuntimeException("说话角色不能为空");
        }
        CardRc rc = cardRcMapper.getById(rcId);
        if (rc == null) {
            throw new RuntimeException("RC 不存在");
        }
        // 说话角色 == 发起人 → 右侧
        return rc.getRoleId() != null && rc.getRoleId().equals(speakerId)
                ? DialogueSideEnum.RIGHT.getValue()
                : DialogueSideEnum.LEFT.getValue();
    }

    /**
     * 校验当前用户是否有权操作指定 RC，并返回其实体
     *
     * @param id RC 主键
     * @throws RuntimeException 资源不存在
     * @throws ForbiddenException 无权操作
     */
    private void checkOperable(Long id) {
        if (id == null) {
            throw new RuntimeException("RC ID不能为空");
        }
        CardRc exist = cardRcMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("RC 不存在");
        }
        Boolean can = cardRcMapper.canOperate(id, BaseContext.getCurrentId(), BaseContext.getCurrentRole());
        if (!Boolean.TRUE.equals(can)) {
            throw new ForbiddenException("无权操作该 RC");
        }
    }
}
