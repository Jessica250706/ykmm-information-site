package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用字典项
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictItemVO implements Serializable {

    private Integer value;

    private String label;
}