package com.xq.service.impl;

import com.xq.constant.RedisConstant;
import com.xq.entity.CardCategory;
import com.xq.entity.Sticker;
import com.xq.enumeration.AttributeEnum;
import com.xq.enumeration.RarityEnum;
import com.xq.enumeration.StoryCategoryTypeEnum;
import com.xq.mapper.CardCategoryMapper;
import com.xq.mapper.StickerMapper;
import com.xq.service.CommonService;
import com.xq.vo.DictItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private CardCategoryMapper cardCategoryMapper;

    @Autowired
    private StickerMapper stickerMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // ---------------------------------------------------
    // 卡面类别字典
    // ---------------------------------------------------
    @Override
    public List<DictItemVO> getCardCategoryDict() {
        String key = RedisConstant.DICT_CARD_CATEGORY;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("卡面类别字典命中缓存");
            return (List<DictItemVO>) cached;
        }

        // 2. 查数据库
        List<CardCategory> list = cardCategoryMapper.listAll();
        List<DictItemVO> result = new ArrayList<>();
        if (list != null) {
            result = list.stream()
                    .map(c -> new DictItemVO(c.getId(), c.getName()))
                    .collect(Collectors.toList());
        }

        // 3. 写入缓存
        redisTemplate.opsForValue().set(
                key, result,
                RedisConstant.DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }

    // ---------------------------------------------------
    // 属性字典（枚举，无缓存）
    // ---------------------------------------------------
    @Override
    public List<DictItemVO> getAttributeDict() {
        return AttributeEnum.listAll().stream()
                .map(e -> new DictItemVO(e.getValue(), e.getLabel()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------
    // 稀有度字典（枚举，无缓存）
    // ---------------------------------------------------
    @Override
    public List<DictItemVO> getRarityDict() {
        return RarityEnum.listAll().stream()
                .map(e -> new DictItemVO(e.getValue(), e.getLabel()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------
    // 剧情分类类型字典（枚举，无缓存）
    // ---------------------------------------------------
    @Override
    public List<DictItemVO> getStoryCategoryTypeDict() {
        return StoryCategoryTypeEnum.listAll().stream()
                .map(e -> new DictItemVO(e.getValue(), e.getLabel()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------
    // 表情包列表
    // ---------------------------------------------------
    @Override
    public List<Sticker> listStickers(String keyword, Integer stickerType) {
        String cacheKey = buildStickerListKey(keyword, stickerType);

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("表情包列表命中缓存：{}", cacheKey);
            return (List<Sticker>) cached;
        }

        // 2. 查数据库
        List<Sticker> list = stickerMapper.listByCondition(keyword, stickerType);
        if (list == null) {
            list = new ArrayList<>();
        }

        // 3. 写缓存
        redisTemplate.opsForValue().set(
                cacheKey, list,
                RedisConstant.DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return list;
    }

    // ---------------------------------------------------
    // 表情包详情
    // ---------------------------------------------------
    @Override
    public Sticker getStickerById(Long id) {
        String cacheKey = RedisConstant.DICT_STICKER_DETAIL_PREFIX + id;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("表情包详情命中缓存：{}", id);
            return (Sticker) cached;
        }

        // 2. 查数据库
        Sticker sticker = stickerMapper.getById(id);
        if (sticker == null) {
            log.warn("表情包不存在，id={}", id);
            return null;
        }

        // 3. 写缓存
        redisTemplate.opsForValue().set(
                cacheKey, sticker,
                RedisConstant.DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return sticker;
    }

    // ---------------------------------------------------
    // 构建表情包列表缓存 key
    // ---------------------------------------------------
    private String buildStickerListKey(String keyword, Integer stickerType) {
        String kw = (keyword == null ? "" : keyword.trim());
        String type = (stickerType == null ? "all" : String.valueOf(stickerType));
        return RedisConstant.DICT_STICKER_LIST_PREFIX + type + ":" + kw;
    }
}
