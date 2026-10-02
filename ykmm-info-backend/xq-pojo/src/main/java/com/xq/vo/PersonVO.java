package com.xq.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人物列表/详情返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonVO implements Serializable {

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
     * 声优
     */
    private String cv;

    /**
     * 1偶像 2经纪人
     */
    private Integer personType;

    /**
     * 文本：1偶像 2经纪人
     */
    private String personTypeLabel;

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
     * 1-A 2-B 3-O 4-AB 5-其他
     */
    private Integer bloodType;

    /**
     * 文本：1-A 2-B 3-O 4-AB 5-其他
     */
    private String bloodTypeLabel;

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
     * 展示图片列表
     */
    private List<String> images;

    /**
     * 所属团体（偶像）
     */
    private List<PersonIdolGroupVO> groups;

    /**
     * 所属公司（经纪人）
     */
    private List<AgencyVO> agencies;
}