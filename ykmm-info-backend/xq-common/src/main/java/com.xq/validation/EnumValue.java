package com.xq.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 枚举值校验注解
 * <p>
 * 要求目标枚举类：
 * - 有 public Integer getValue() 方法
 * - 或有 public static boolean isValid(Integer) 方法
 * <p>
 * 用法：
 *
 * @EnumValue(enumClass = CardMaxRarityEnum.class)
 * private Integer maxRarity;
 * @EnumValue(enumClass = CardAttributeEnum.class, nullable = true)
 * private Integer attribute;   // null 允许
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
public @interface EnumValue {

    /**
     * 目标枚举类
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 是否允许 null（默认不允许）
     */
    boolean nullable() default false;

    /**
     * 校验失败提示
     */
    String message() default "枚举值不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
