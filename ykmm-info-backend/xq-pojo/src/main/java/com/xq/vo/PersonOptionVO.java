package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人物全部返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonOptionVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 中文名
     */
    private String nameCn;

    /**
     * 日文名
     */
    private String nameJp;

    /**
     * 罗马音
     */
    private String nameRomaji;
}
