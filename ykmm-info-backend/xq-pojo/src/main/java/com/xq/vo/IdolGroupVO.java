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
public class IdolGroupVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 团体名
     */
    private String name;

    /**
     * 所属经纪公司
     */
    private Long agencyId;

    /**
     * 所属经纪公司名
     */
    private String agencyName;
}
