package com.xq.constant;

import java.time.Duration;

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

    public static final String REGISTER_CODE_KEY_PREFIX = "auth:register:code:";
    public static final String REGISTER_CODE_LIMIT_KEY_PREFIX = "auth:register:code:limit:";

    public static final String REBIND_OLD_CODE_KEY_PREFIX = "auth:rebind:code:old:";
    public static final String REBIND_NEW_CODE_KEY_PREFIX = "auth:rebind:code:new:";
    public static final String REBIND_CODE_LIMIT_KEY_PREFIX = "auth:rebind:code:limit:";

    public static final String PASSWORD_RESET_CODE_KEY_PREFIX = "auth:password:reset:code:";
    public static final String PASSWORD_RESET_CODE_LIMIT_KEY_PREFIX = "auth:password:reset:code:limit:";

    /**
     * 字典缓存过期时间（小时）
     */
    public static final Long DICT_CACHE_TTL_HOURS = 1L;

    public static final Duration REGISTER_CODE_TTL = Duration.ofMinutes(5);
    public static final Duration REGISTER_CODE_SEND_LIMIT = Duration.ofSeconds(60);
}
