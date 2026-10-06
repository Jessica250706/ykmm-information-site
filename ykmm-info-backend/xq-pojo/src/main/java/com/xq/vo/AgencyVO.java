package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgencyVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 公司名
     */
    private String name;
}
