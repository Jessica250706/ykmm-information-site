package com.xq.dto;

import lombok.Data;

import java.util.List;

/**
 * 对话句子排序请求
 */
@Data
public class DialogueLineSortDTO {

    /**
     * 句子ID顺序
     */
    private List<Long> lineIds;
}
