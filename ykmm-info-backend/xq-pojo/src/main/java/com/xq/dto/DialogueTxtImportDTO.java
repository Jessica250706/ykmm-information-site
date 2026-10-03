package com.xq.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * txt 解析结果确认导入
 */
@Data
public class DialogueTxtImportDTO implements Serializable {

    /**
     * 确认后的句子列表
     */
    private List<DialogueLineDTO> lines;
}
