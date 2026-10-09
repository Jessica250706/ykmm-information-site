package com.xq.service;

import com.xq.dto.CardDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.CardEpisodeVO;
import com.xq.vo.CardVO;

/**
 * 卡面服务
 */
public interface CardService {

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<CardVO> pageQuery(CardPageQueryDTO query);

    /**
     * 查询卡面详情（不含对话）
     *
     * @param id 卡面ID
     * @return 卡面详情
     */
    CardVO getById(Long id);

    /**
     * 查询某一话的对话
     *
     * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter
     * @param sourceId   来源ID：card_rc.id / card_rtv.id / card_rabitter.id
     * @return 话详情（含对话版本）
     */
    CardEpisodeVO getEpisodeDialogue(Integer sourceType, Long sourceId);

    /**
     * 新增卡面
     *
     * @param dto 新增参数
     * @return 新增后的主键
     */
    Long create(CardDTO dto);

    /**
     * 编辑卡面
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    void update(Long id, CardDTO dto);

    /**
     * 删除卡面
     *
     * @param id 主键
     */
    void delete(Long id);
}
