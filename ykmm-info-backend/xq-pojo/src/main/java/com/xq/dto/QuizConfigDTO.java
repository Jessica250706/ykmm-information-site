package com.xq.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 答题配置 DTO
 */
@Data
public class QuizConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否开启答题门槛：1-开启 0-关闭
     */
    @NotNull(message = "是否开启不能为空")
    private Integer enabled;

    /**
     * 每次答题的总题数（含固定题）
     */
    @NotNull(message = "总题数不能为空")
    @Min(value = 1, message = "总题数至少为 1")
    private Integer totalCount;

    /**
     * 通过所需答对题数
     */
    @NotNull(message = "通过题数不能为空")
    @Min(value = 1, message = "通过题数至少为 1")
    private Integer passCount;

    /**
     * 最大答题次数：0-不限
     */
    @NotNull(message = "最大答题次数不能为空")
    @Min(value = 0, message = "最大答题次数不能为负")
    private Integer maxAttempts;
}
