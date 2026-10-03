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
     * 语言：中文
     */
    public static final int LANG_CN = 1;

    /**
     * 语言：日文
     */
    public static final int LANG_JP = 2;

    /**
     * 形式：文字
     */
    public static final int FORMAT_TEXT = 1;

    /**
     * 形式：图片
     */
    public static final int FORMAT_IMAGE = 2;

    /**
     * 来源：剧情
     */
    public static final int SOURCE_STORY = 1;

    /**
     * 来源：卡面 RTV
     */
    public static final int SOURCE_RTV = 2;

    /**
     * 来源：卡面 RC
     */
    public static final int SOURCE_RC = 3;
}
