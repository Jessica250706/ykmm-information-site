package com.xq.service;

import com.xq.dto.CardDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.dto.CardReviewDTO;
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

    /**
     * 判断当前登录用户能否为指定卡面新增 RC、RTV、Rabitter
     *
     * @param cardId 卡面ID
     * @return true 表示可以新增
     */
    boolean canCreate(Long cardId);

    /**
     * 断言当前用户可以对指定卡面进行写操作（新增/编辑话数）。
     * <p>
     * 规则：
     * <ul>
     *   <li>管理员：允许</li>
     *   <li>普通用户：card.creator_id = 当前用户</li>
     *   <li>游客：拒绝</li>
     * </ul>
     *
     * @param cardId 卡面ID
     * @throws com.xq.exception.ForbiddenException 无权操作
     */
    void assertWritable(Long cardId);

    /**
     * 用户在卡面下新增/编辑话数后，将卡面重新置为「待审核」。
     * <p>
     * 管理员操作时不改变卡面状态（管理员本身是审核人，无需自我审核）。
     *
     * @param cardId 卡面ID
     */
    void markPendingIfNeeded(Long cardId);

    /**
     * 审核卡面
     *
     * @param id  卡面ID
     * @param dto 审核参数（通过 / 拒绝 + 备注）
     */
    void review(Long id, CardReviewDTO dto);

    /**
     * 分页查询当前用户创建的卡面
     */
    PageResult<CardVO> pageMyCards(CardPageQueryDTO query);
}
