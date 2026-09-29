package com.xq.constant;

/**
 * Redis 常量类
 */
public class RedisConstant {

    /**
     * 卡面类别字典缓存 key
     */
    public static final String DICT_CARD_CATEGORY = "dict:card_category";

    /**
     * 表情包列表缓存 key 前缀
     */
    public static final String DICT_STICKER_LIST_PREFIX = "dict:sticker:list:";

    /**
     * 表情包详情缓存 key 前缀
     */
    public static final String DICT_STICKER_DETAIL_PREFIX = "dict:sticker:detail:";

    /**
     * 字典缓存过期时间（小时）
     */
    public static final Long DICT_CACHE_TTL_HOURS = 1L;
}
