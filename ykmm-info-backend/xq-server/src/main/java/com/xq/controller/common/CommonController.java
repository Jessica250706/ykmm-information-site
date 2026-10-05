package com.xq.controller.common;

import com.xq.entity.Sticker;
import com.xq.result.Result;
import com.xq.service.CommonService;
import com.xq.utils.AliOssUtil;
import com.xq.vo.DictItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController("CommonController")
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private CommonService commonService;

    /**
     * 允许上传的图片后缀
     */
    private static final List<String> ALLOWED_EXT =
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");

    /**
     * 文件大小上限：10MB
     */
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    // ===================================================
    // 字典接口
    // ===================================================

    /**
     * 获取卡面类别字典
     * （游客可访问）
     *
     * @return 卡面类别字典列表
     */
    @GetMapping("/dict/card-category")
    public Result<List<DictItemVO>> getCardCategoryDict() {
        return Result.success(commonService.getCardCategoryDict());
    }

    /**
     * 获取属性字典（Shout/Beat/Melody）
     * （游客可访问）
     *
     * @return 属性字典列表
     */
    @GetMapping("/dict/attributes")
    public Result<List<DictItemVO>> getAttributeDict() {
        return Result.success(commonService.getAttributeDict());
    }

    /**
     * 获取稀有度字典（SSR/UR）
     * （游客可访问）
     *
     * @return 稀有度字典列表
     */
    @GetMapping("/dict/rarity")
    public Result<List<DictItemVO>> getRarityDict() {
        return Result.success(commonService.getRarityDict());
    }

    /**
     * 获取剧情分类类型字典
     * （游客可访问）
     *
     * @return 剧情分类类型字典列表
     */
    @GetMapping("/dict/story-category-type")
    public Result<List<DictItemVO>> getStoryCategoryTypeDict() {
        return Result.success(commonService.getStoryCategoryTypeDict());
    }

    // ===================================================
    // 表情包接口
    // ===================================================

    /**
     * 获取表情包列表
     * （游客可访问）
     *
     * @param keyword     搜索关键词（可选）
     * @param stickerType 表情包类型（可选）
     * @return 表情包列表
     */
    @GetMapping("/sticker")
    public Result<List<Sticker>> listStickers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer stickerType) {
        return Result.success(commonService.listStickers(keyword, stickerType));
    }

    /**
     * 获取表情包详情
     * （游客可访问）
     *
     * @param id 表情包ID
     * @return 表情包详情
     */
    @GetMapping("/sticker/{id}")
    public Result<Sticker> getSticker(@PathVariable Long id) {
        Sticker sticker = commonService.getStickerById(id);
        if (sticker == null) {
            return Result.error("表情包不存在");
        }
        return Result.success(sticker);
    }

    // ===================================================
    // 文件上传
    // ===================================================

    /**
     * 上传图片/文件，返回URL
     * （用户/管理员可访问）
     *
     * @param file 上传的文件
     * @return 上传后的文件访问URL
     * @throws IOException 文件读取异常
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}", file);

        // 1. 校验非空
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 2. 校验大小
        if (file.getSize() > MAX_SIZE) {
            return Result.error("文件大小不能超过 10MB");
        }

        // 3. 校验后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件名不合法");
        }
        String extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();
        if (!ALLOWED_EXT.contains(extension)) {
            return Result.error("只允许上传图片文件");
        }

        // 4. 构造唯一文件名
        String objectName = UUID.randomUUID() + extension;

        // 5. 上传
        String filePath = aliOssUtil.upload(file.getBytes(), objectName);
        log.info("文件上传成功：{}", filePath);
        return Result.success(filePath);
    }
}
