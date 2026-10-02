package com.xq.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据
     */
    private List<T> records;

    public PageResult(List<T> records, Long total) {
        this.records = records;
        this.total = total;
    }

    public static <T> PageResult<T> empty(Long total) {
        return new PageResult<>(Collections.emptyList(), total);
    }

}
