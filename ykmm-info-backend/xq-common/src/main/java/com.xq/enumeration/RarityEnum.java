package com.xq.enumeration;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum RarityEnum {

    SSR(1, "SSR"),
    UR(2, "UR");

    private final Integer value;
    private final String label;

    RarityEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static List<RarityEnum> listAll() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
