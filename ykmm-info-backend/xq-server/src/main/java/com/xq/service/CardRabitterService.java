package com.xq.service;

import com.xq.dto.CardRabitterDTO;
import com.xq.vo.CardRabitterVO;

import java.util.List;

/**
 * 卡面 Rabitter 服务
 */
public interface CardRabitterService {

    /**
     * 查询卡面下的 Rabitter 列表
     *
     * @param cardId 卡面ID
     * @return Rabitter 列表
     */
    List<CardRabitterVO> listByCardId(Long cardId);

    /**
     * 查询详情
     *
     * @param id 主键
     * @return Rabitter 详情
     */
    CardRabitterVO getById(Long id);

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    Long create(CardRabitterDTO dto);

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, CardRabitterDTO dto);

    /**
     * 删除
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 根据卡面ID删除
     *
     * @param cardId 卡面ID
     */
    void deleteByCardId(Long cardId);
}
