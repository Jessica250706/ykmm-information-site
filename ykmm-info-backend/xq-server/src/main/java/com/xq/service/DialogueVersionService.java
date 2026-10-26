package com.xq.service;

import com.xq.dto.DialogueVersionDTO;
import com.xq.dto.DialogueVersionPageQueryDTO;
import com.xq.dto.DialogueVersionReviewDTO;
import com.xq.result.PageResult;
import com.xq.vo.DialogueVersionOptionVO;
import com.xq.vo.DialogueVersionVO;

import java.util.List;

/**
 * 对话版本服务
 */
public interface DialogueVersionService {

    /**
     * 分页查询对话版本
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<DialogueVersionVO> pageQuery(DialogueVersionPageQueryDTO query);

    /**
     * 查询某来源下的全部版本
     *
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     * @return 版本列表
     */
    List<DialogueVersionVO> listBySource(Integer sourceType, Long sourceId);

    /**
     * 查询版本详情
     *
     * @param versionId 版本ID
     * @return 版本详情
     */
    DialogueVersionVO detail(Long versionId);

    /**
     * 创建版本
     *
     * @param dto 创建参数
     * @return 新版本ID
     */
    Long create(DialogueVersionDTO dto);

    /**
     * 删除版本
     *
     * @param versionId 版本ID
     */
    void delete(Long versionId);

    /**
     * 查询全部文字版本选项
     *
     * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     * @param sourceId   来源主键
     * @return 选项列表
     */
    List<DialogueVersionOptionVO> listTextVersionOptions(Integer sourceType, Long sourceId);

    /**
     * 查询全部图片版本选项
     *
     * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     * @param sourceId   来源主键
     * @return 选项列表
     */
    List<DialogueVersionOptionVO> listImageVersionOptions(Integer sourceType, Long sourceId);

    /**
     * 查询全部版本选项
     *
     * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
     * @param sourceId   来源主键
     * @return 选项列表
     */
    List<DialogueVersionOptionVO> listVersionOptions(Integer sourceType, Long sourceId);

    /**
     * 分页查询当前用户创建的版本
     */
    PageResult<DialogueVersionVO> pageMyVersions(DialogueVersionPageQueryDTO query);

    /**
     * 审核对话版本
     * <p>
     * 规则：
     * <ul>
     *   <li>仅管理员可操作</li>
     *   <li>仅 PENDING（待审核）状态可审核</li>
     *   <li>审核结果只允许「通过 1 / 拒绝 3」</li>
     *   <li>拒绝时必须填写审核备注</li>
     *   <li>审核完成后向版本创建者推送消息；管理员自审（自己创建自己审）不发消息</li>
     * </ul>
     *
     * @param id  版本ID
     * @param dto 审核参数
     */
    void review(Long id, DialogueVersionReviewDTO dto);
}
