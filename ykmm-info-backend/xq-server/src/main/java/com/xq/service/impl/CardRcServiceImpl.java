package com.xq.service.impl;

import com.xq.dto.CardRcDTO;
import com.xq.entity.CardRc;
import com.xq.enums.DialogueSideEnum;
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
        if (cardId == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        return cardRcMapper.listVOByCardId(cardId);
    }

    /**
     * 查询详情
     */
    @Override
    public CardRcVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("RC ID不能为空");
        }
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
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getCardId() == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        if (dto.getRoleId() == null) {
            throw new RuntimeException("RC 发起人不能为空");
        }
        if (dto.getEpisodeNo() == null) {
            throw new RuntimeException("话数不能为空");
        }

        CardRc rc = new CardRc();
        BeanUtils.copyProperties(dto, rc);
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
        if (id == null) {
            throw new RuntimeException("RC ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        CardRc exist = cardRcMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("RC 不存在");
        }
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
        if (id == null) {
            throw new RuntimeException("RC ID不能为空");
        }
        CardRc exist = cardRcMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("RC 不存在");
        }
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
}
