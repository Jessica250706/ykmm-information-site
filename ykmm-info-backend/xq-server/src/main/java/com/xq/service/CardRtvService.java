package com.xq.service;

import com.xq.dto.CardRtvDTO;
import com.xq.vo.CardRtvVO;

import java.util.List;

/**
 * 卡面 RTV 服务
 */
public interface CardRtvService {

    /**
     * 查询卡面下的 RTV 列表
     *
     * @param cardId 卡面ID
     * @return RTV 列表
     */
    List<CardRtvVO> listByCardId(Long cardId);

    /**
     * 查询详情
     *
     * @param id 主键
     * @return RTV 详情
     */
    CardRtvVO getById(Long id);

    /**
     * 新增
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    Long create(CardRtvDTO dto);

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, CardRtvDTO dto);

    /**
     * 删除
     *
     * @param id 主键
     */
    void delete(Long id);
}
