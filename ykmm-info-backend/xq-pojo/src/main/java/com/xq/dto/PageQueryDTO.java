package com.xq.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基类
 * 所有需要分页的查询 DTO 继承此类
 */
@Data
public class PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码，从 1 开始
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认 10，最大 100
     */
    private Integer pageSize = 10;

    /**
     * 页码兜底：null 或 <1 时置为 1
     */
    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    /**
     * 每页条数兜底：null 或 <1 时置为 10；超过 100 时置为 100
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        if (pageSize > 100) {
            return 100;
        }
        return pageSize;
    }
}
