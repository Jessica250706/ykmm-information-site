package com.xq.enumeration;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum StoryCategoryTypeEnum {

    MAIN(1, "主线"),
    RAINBOW_CITY(2, "彩虹城"),
    SPECIAL(3, "特别篇"),
    ACTIVITY(4, "活动篇"),
    DRAMA(5, "戏剧篇");

    private final Integer value;
    private final String label;

    StoryCategoryTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static List<StoryCategoryTypeEnum> listAll() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
