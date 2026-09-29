package com.xq.service;

import com.xq.entity.Sticker;
import com.xq.vo.DictItemVO;

import java.util.List;

public interface CommonService {

    /**
     * 卡面类别字典
     */
    List<DictItemVO> getCardCategoryDict();

    /**
     * 属性字典
     */
    List<DictItemVO> getAttributeDict();

    /**
     * 稀有度字典
     */
    List<DictItemVO> getRarityDict();

    /**
     * 剧情分类类型字典
     */
    List<DictItemVO> getStoryCategoryTypeDict();

    /**
     * 表情包列表
     */
    List<Sticker> listStickers(String keyword, Integer stickerType);

    /**
     * 表情包详情
     */
    Sticker getStickerById(Long id);
}
