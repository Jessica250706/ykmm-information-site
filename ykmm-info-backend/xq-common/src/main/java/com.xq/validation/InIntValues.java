package com.xq.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 校验目标整数必须是指定集合中的一个值
 *
 * 用法：
 *   @InIntValues({1, 3})
 *   private Integer status;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = InIntValuesValidator.class)
public @interface InIntValues {

    /** 允许的值集合 */
    int[] value();

    /** 是否允许 null（默认不允许） */
    boolean nullable() default false;

    /** 校验失败提示 */
    String message() default "值不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
