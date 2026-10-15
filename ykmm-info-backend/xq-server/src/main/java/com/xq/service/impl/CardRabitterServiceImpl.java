package com.xq.service.impl;

import com.xq.context.BaseContext;
import com.xq.dto.CardRabitterDTO;
import com.xq.entity.CardRabitter;
import com.xq.exception.ForbiddenException;
import com.xq.mapper.CardRabitterMapper;
import com.xq.service.CardRabitterService;
import com.xq.vo.CardRabitterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡面 Rabitter 服务实现
 */
@Service
@Slf4j
public class CardRabitterServiceImpl implements CardRabitterService {

    @Autowired
    private CardRabitterMapper cardRabitterMapper;

    /**
     * 查询卡面下的 Rabitter 列表
     *
     * @param cardId 卡面ID
     * @return Rabitter 列表
     */
    @Override
    public List<CardRabitterVO> listByCardId(Long cardId) {
        List<CardRabitter> list = cardRabitterMapper.listByCardId(cardId);

        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();

        List<CardRabitterVO> voList = new ArrayList<>(list.size());
        for (CardRabitter rabitter : list) {
            CardRabitterVO vo = toVO(rabitter);
            Boolean can = cardRabitterMapper.canOperate(rabitter.getId(), userId, role);
            vo.setCanOperate(Boolean.TRUE.equals(can));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return Rabitter 详情
     */
    @Override
    public CardRabitterVO getById(Long id) {
        CardRabitter rabitter = cardRabitterMapper.getById(id);
        // Rabitter 必须存在
        if (rabitter == null) {
            throw new RuntimeException("Rabitter 不存在");
        }
        return toVO(rabitter);
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @Override
    @Transactional
    public Long create(CardRabitterDTO dto) {
        CardRabitter rabitter = new CardRabitter();
        BeanUtils.copyProperties(dto, rabitter);
        rabitter.setCreatorId(BaseContext.getCurrentId());
        cardRabitterMapper.insert(rabitter);
        log.info("新增卡面 Rabitter 成功，id={}", rabitter.getId());
        return rabitter.getId();
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, CardRabitterDTO dto) {
        checkOperable(id);
        CardRabitter rabitter = new CardRabitter();
        BeanUtils.copyProperties(dto, rabitter);
        rabitter.setId(id);
        cardRabitterMapper.update(rabitter);
        log.info("编辑卡面 Rabitter 成功，id={}", id);
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        checkOperable(id);
        cardRabitterMapper.deleteById(id);
        log.info("删除卡面 Rabitter 成功，id={}", id);
    }

    /**
     * 根据卡面ID删除
     *
     * @param cardId 卡面ID
     */
    @Override
    @Transactional
    public void deleteByCardId(Long cardId) {
        // 卡面ID不能为空
        if (cardId == null) {
            throw new RuntimeException("卡面ID不能为空");
        }
        cardRabitterMapper.deleteByCardId(cardId);
        log.info("根据卡面ID删除 Rabitter 成功，cardId={}", cardId);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 实体 → VO
     *
     * @param rabitter 实体
     * @return VO
     */
    private CardRabitterVO toVO(CardRabitter rabitter) {
        if (rabitter == null) return null;
        CardRabitterVO vo = new CardRabitterVO();
        BeanUtils.copyProperties(rabitter, vo);
        return vo;
    }

    /**
     * 校验当前用户是否有权操作指定 Rabitter，并返回其实体
     *
     * @param id Rabitter 主键
     * @throws RuntimeException   资源不存在
     * @throws ForbiddenException 无权操作
     */
    private void checkOperable(Long id) {
        if (id == null) {
            throw new RuntimeException("Rabitter ID不能为空");
        }
        CardRabitter exist = cardRabitterMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("Rabitter 不存在");
        }
        Boolean can = cardRabitterMapper.canOperate(id, BaseContext.getCurrentId(), BaseContext.getCurrentRole());
        if (!Boolean.TRUE.equals(can)) {
            throw new ForbiddenException("无权操作该 Rabitter");
        }
    }
}
