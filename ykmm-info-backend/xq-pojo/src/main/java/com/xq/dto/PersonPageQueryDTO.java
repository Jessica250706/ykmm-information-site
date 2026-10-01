package com.xq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 人物分页查询条件
 */
@Data
public class PersonPageQueryDTO extends PageQueryDTO implements Serializable {

    /**
     * 关键字：中文名 / 日文名 / 罗马音
     */
    private String keyword;

    /**
     * 1偶像 2经纪人
     */
    private Integer personType;

    /**
     * 所属团体ID（仅偶像有效）
     */
    private Long groupId;

    /**
     * 所属公司ID（仅经纪人有效）
     */
    private Long agencyId;
}
