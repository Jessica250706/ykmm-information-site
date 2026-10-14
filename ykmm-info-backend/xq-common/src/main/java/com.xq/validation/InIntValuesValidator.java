package com.xq.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

/**
 * @InIntValues 校验器
 */
public class InIntValuesValidator implements ConstraintValidator<InIntValues, Integer> {

    private int[] allowed;
    private boolean nullable;

    @Override
    public void initialize(InIntValues annotation) {
        this.allowed = annotation.value();
        this.nullable = annotation.nullable();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return nullable;
        }
        return Arrays.stream(allowed).anyMatch(v -> v == value);
    }
}
