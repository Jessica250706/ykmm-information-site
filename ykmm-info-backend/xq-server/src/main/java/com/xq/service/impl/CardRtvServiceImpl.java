package com.xq.service.impl;

import com.xq.context.BaseContext;
import com.xq.dto.CardRtvDTO;
import com.xq.entity.CardRtv;
import com.xq.exception.ForbiddenException;
import com.xq.mapper.CardRtvMapper;
import com.xq.service.CardRtvService;
import com.xq.service.CardService;
import com.xq.vo.CardRtvVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡面 RTV 服务实现
 */
@Service
@Slf4j
public class CardRtvServiceImpl implements CardRtvService {

    @Autowired
    private CardRtvMapper cardRtvMapper;
    @Autowired
    private CardService cardService;

    /**
     * 查询卡面下的 RTV 列表
     *
     * @param cardId 卡面ID
     * @return RTV 列表
     */
    @Override
    public List<CardRtvVO> listByCardId(Long cardId) {
        List<CardRtv> list = cardRtvMapper.listByCardId(cardId);

        Long userId = BaseContext.getCurrentId();
        Integer role = BaseContext.getCurrentRole();

        List<CardRtvVO> voList = new ArrayList<>(list.size());
        for (CardRtv rtv : list) {
            CardRtvVO vo = toVO(rtv);
            Boolean can = cardRtvMapper.canOperate(rtv.getId(), userId, role);
            vo.setCanOperate(Boolean.TRUE.equals(can));
            voList.add(vo);
        }

        return voList;
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return RTV 详情
     */
    @Override
    public CardRtvVO getById(Long id) {
        CardRtv rtv = cardRtvMapper.getById(id);
        if (rtv == null) {
            throw new RuntimeException("RTV 不存在");
        }
        return toVO(rtv);
    }

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    @Override
    @Transactional
    public Long create(CardRtvDTO dto) {
        cardService.assertWritable(dto.getCardId());

        CardRtv rtv = new CardRtv();
        BeanUtils.copyProperties(dto, rtv);
        rtv.setCreatorId(BaseContext.getCurrentId());
        cardRtvMapper.insert(rtv);

        cardService.markPendingIfNeeded(dto.getCardId());

        log.info("新增卡面 RTV 成功，id={}", rtv.getId());
        return rtv.getId();
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, CardRtvDTO dto) {
        CardRtv exist = checkOperable(id);
        cardService.assertWritable(exist.getCardId());

        CardRtv rtv = new CardRtv();
        BeanUtils.copyProperties(dto, rtv);
        rtv.setId(id);
        rtv.setCardId(exist.getCardId());  // 防止越权迁移

        cardRtvMapper.update(rtv);

        cardService.markPendingIfNeeded(exist.getCardId());

        log.info("编辑卡面 RTV 成功，id={}", id);
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
        cardRtvMapper.deleteById(id);
        log.info("删除卡面 RTV 成功，id={}", id);
    }

    /* ---------------- 私有方法 ---------------- */

    /**
     * 实体 → VO
     *
     * @param rtv 实体
     * @return VO
     */
    private CardRtvVO toVO(CardRtv rtv) {
        if (rtv == null) return null;
        CardRtvVO vo = new CardRtvVO();
        BeanUtils.copyProperties(rtv, vo);
        return vo;
    }

    /**
     * 校验当前用户是否有权操作指定 RTV，并返回其实体
     *
     * @param id RTV 主键
     * @throws RuntimeException 资源不存在
     * @throws ForbiddenException 无权操作
     */
    private CardRtv checkOperable(Long id) {
        if (id == null) {
            throw new RuntimeException("RTV ID不能为空");
        }
        CardRtv exist = cardRtvMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("RTV 不存在");
        }
        Boolean can = cardRtvMapper.canOperate(id, BaseContext.getCurrentId(), BaseContext.getCurrentRole());
        if (!Boolean.TRUE.equals(can)) {
            throw new ForbiddenException("无权操作该 RTV");
        }
        return exist;
    }
}
