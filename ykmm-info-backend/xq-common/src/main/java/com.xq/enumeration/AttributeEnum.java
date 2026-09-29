package com.xq.enumeration;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum AttributeEnum {

    SHOUT(1, "Shout"),
    BEAT(2, "Beat"),
    MELODY(3, "Melody");

    private final Integer value;
    private final String label;

    AttributeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static List<AttributeEnum> listAll() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
