package com.xq.service;

import com.xq.dto.CardDTO;
import com.xq.dto.CardPageQueryDTO;
import com.xq.result.PageResult;
import com.xq.vo.CardVO;

/**
 * 卡面服务
 */
public interface CardService {
    /** 分页查询 */
    PageResult<CardVO> pageQuery(CardPageQueryDTO query);
    /** 查询详情 */
    CardVO getById(Long id);
    /** 新增 */
    Long create(CardDTO dto);
    /** 编辑 */
    void update(Long id, CardDTO dto);
    /** 删除 */
    void delete(Long id);
}
