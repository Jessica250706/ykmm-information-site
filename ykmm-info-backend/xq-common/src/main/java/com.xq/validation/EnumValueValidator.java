package com.xq.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;

/**
 * @EnumValue 校验器
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, Integer> {

    private Class<? extends Enum<?>> enumClass;
    private boolean nullable;

    @Override
    public void initialize(EnumValue annotation) {
        this.enumClass = annotation.enumClass();
        this.nullable = annotation.nullable();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // 允许 null
        if (value == null) {
            return nullable;
        }

        // 优先尝试反射调用 isValid(Integer) 静态方法
        try {
            Method isValid = enumClass.getMethod("isValid", Integer.class);
            Object result = isValid.invoke(null, value);
            if (result instanceof Boolean b) {
                return b;
            }
        } catch (NoSuchMethodException ignored) {
            // 没有 isValid 方法，走 getValue 对比
        } catch (Exception e) {
            // 反射异常，继续走 getValue 对比
        }

        // 退化为遍历枚举，对比 getValue()
        try {
            Method getValue = enumClass.getMethod("getValue");
            for (Enum<?> e : enumClass.getEnumConstants()) {
                Object v = getValue.invoke(e);
                if (value.equals(v)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("枚举类 " + enumClass.getName()
                    + " 缺少 getValue() 或 isValid(Integer) 方法", e);
        }

        return false;
    }
}
