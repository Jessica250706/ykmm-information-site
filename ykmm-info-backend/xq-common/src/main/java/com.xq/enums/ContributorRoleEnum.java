package com.xq.enums;

import lombok.Getter;

/**
 * 对话版本贡献者角色
 */
@Getter
public enum ContributorRoleEnum {

    /**
     * 内容作者：版本内容的实际归属人
     */
    AUTHOR(1, "内容作者"),

    /**
     * 代传管理员：管理员代替用户上传时记录的操作人
     */
    DELEGATE(2, "代传管理员"),

    /**
     * 协作者：参与协作编辑的用户
     */
    COLLABORATOR(3, "协作者");

    private final Integer value;
    private final String label;

    ContributorRoleEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据值查找枚举
     *
     * @param value 枚举值
     * @return 对应枚举，找不到返回 null
     */
    public static ContributorRoleEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ContributorRoleEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据值取标签
     *
     * @param value 枚举值
     * @return 标签，无效时返回"未知"
     */
    public static String getLabel(Integer value) {
        ContributorRoleEnum e = of(value);
        return e == null ? "未知" : e.label;
    }

    /**
     * 校验枚举值是否合法
     *
     * @param value 待校验值
     * @return true 合法，false 不合法
     */
    public static boolean isValid(Integer value) {
        return of(value) != null;
    }
}
