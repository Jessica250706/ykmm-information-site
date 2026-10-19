package com.xq.controller.common;

import com.xq.result.Result;
import com.xq.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 通用 - 文件上传
 */
@RestController("CommonUploadController")
@RequestMapping("/common")
@Slf4j
public class UploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /** 允许上传的图片后缀 */
    private static final List<String> ALLOWED_EXT =
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");

    /** 文件大小上限：10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    /**
     * 上传图片/文件，返回 URL
     *
     * @param file 上传的文件
     * @return 上传后的文件访问 URL
     * @throws IOException 文件读取异常
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}", file);

        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.error("文件大小不能超过 10MB");
        }

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

        String objectName = UUID.randomUUID() + extension;
        String filePath = aliOssUtil.upload(file.getBytes(), objectName);
        log.info("文件上传成功：{}", filePath);
        return Result.success(filePath);
    }
}
