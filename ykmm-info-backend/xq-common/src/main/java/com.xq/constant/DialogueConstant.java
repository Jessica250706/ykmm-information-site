package com.xq.constant;

/**
 * 对话相关常量
 */
public class DialogueConstant {

    /**
     * txt 文件最大大小：2MB
     */
    public static final long TXT_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * txt 文件后缀
     */
    public static final String TXT_EXTENSION = ".txt";

    /**
     * 说话人正则：角色名 + 冒号
     */
    public static final String SPEAKER_PATTERN = "^([^：:\\n]{1,30})[：:]\\s*$";

    /**
     * 表情包标签正则：[xxx表情包]
     */
    public static final String STICKER_PATTERN = "\\[([^\\[\\]]+?表情包)\\]";

    /**
     * 单条消息最大长度
     */
    public static final int MAX_CONTENT_LENGTH = 5000;

    /**
     * 单次批量保存的最大句子数
     */
    public static final int MAX_BATCH_SIZE = 500;

    /**
     * 对话角色：普通
     */
    public static final int DIALOGUE_ROLE_NORMAL = 0;

    /**
     * 对话角色：问句
     */
    public static final int DIALOGUE_ROLE_QUESTION = 1;

    /**
     * 对话角色：回答
     */
    public static final int DIALOGUE_ROLE_ANSWER = 2;

    /**
     * 选项编号行正则，如 "1." "2."
     */
    public static final String OPTION_NUMBER_PATTERN = "^\\d+\\.$";
}
