package com.xq.service;

import com.xq.dto.CardRcDTO;
import com.xq.vo.CardRcVO;

import java.util.List;

/**
 * 卡面 RC 服务
 */
public interface CardRcService {
    /** 查询卡面下的 RC 列表 */
    List<CardRcVO> listByCardId(Long cardId);
    /** 查询详情 */
    CardRcVO getById(Long id);
    /** 新增 */
    Long create(CardRcDTO dto);
    /** 编辑 */
    void update(Long id, CardRcDTO dto);
    /** 删除 */
    void delete(Long id);

    /**
     * 根据 RC 的发起人规则，计算某句话应该落库的 side 值。
     * 规则：说话角色 == 发起人 → side=2，其他角色 → side=1
     *
     * @param rcId      卡面 RC 主键
     * @param speakerId 说话角色ID
     * @return side 值（1 或 2）
     */
    Integer resolveSide(Long rcId, Long speakerId);
}
