package com.xq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人物表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 中文名
     */
    private String nameCn;

    /**
     * 日文名
     */
    private String nameJp;

    /**
     * 罗马音
     */
    private String nameRomaji;

    /**
     * 1偶像 2经纪人
     */
    private Integer personType;

    /**
     * 角色简介
     */
    private String intro;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 生日，如 12-24
     */
    private String birthday;

    /**
     * 鞋码
     */
    private BigDecimal shoeSize;

    /**
     * 身高
     */
    private BigDecimal height;

    /**
     * 血型：1-A 2-B 3-O 4-AB 5-其他
     */
    private Integer bloodType;

    /**
     * 体重
     */
    private BigDecimal weight;

    /**
     * 喜欢的事物
     */
    private String likes;

    /**
     * 不擅长的事物
     */
    private String dislikes;

    /**
     * 应援色
     */
    private String themeColor;

    /**
     * 代表符号
     */
    private String symbol;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 展示图片列表 JSON
     */
    private String images;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
