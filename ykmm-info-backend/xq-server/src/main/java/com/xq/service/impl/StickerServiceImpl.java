package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.context.BaseContext;
import com.xq.dto.StickerDTO;
import com.xq.dto.StickerPageQueryDTO;
import com.xq.entity.Sticker;
import com.xq.enums.StickerTypeEnum;
import com.xq.mapper.StickerGroupMapper;
import com.xq.mapper.StickerMapper;
import com.xq.result.PageResult;
import com.xq.service.StickerService;
import com.xq.vo.StickerGroupVO;
import com.xq.vo.StickerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 表情包Service实现
 */
@Service
@RequiredArgsConstructor
public class StickerServiceImpl implements StickerService {

    private final StickerMapper stickerMapper;
    private final StickerGroupMapper stickerGroupMapper;

    /**
     * 分页查询表情包
     */
    @Override
    public PageResult<StickerVO> pageQuery(StickerPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<StickerVO> list = stickerMapper.pageQuery(queryDTO);
        return new PageResult<>(list.getTotal(), list);
    }

    /**
     * 根据ID查询表情包
     */
    @Override
    public StickerVO getById(Long id) {
        StickerVO stickerVO = stickerMapper.getVOById(id);
        if (stickerVO == null) {
            throw new RuntimeException("表情包不存在");
        }
        return stickerVO;
    }

    /**
     * 新增表情包
     */
    @Override
    @Transactional
    public void save(StickerDTO stickerDTO) {
        validateSticker(stickerDTO);

        StickerGroupVO group = stickerGroupMapper.getVOById(stickerDTO.getGroupId());
        if (group == null) {
            throw new RuntimeException("表情包分组不存在");
        }

        Sticker sticker = Sticker.builder()
                .groupId(stickerDTO.getGroupId())
                .label(stickerDTO.getLabel())
                .imageUrl(stickerDTO.getImageUrl())
                .emoji(stickerDTO.getEmoji())
                .stickerType(stickerDTO.getStickerType())
                .creatorId(BaseContext.getCurrentId())
                .build();

        stickerMapper.insert(sticker);
    }

    /**
     * 更新表情包
     */
    @Override
    @Transactional
    public void update(StickerDTO stickerDTO) {
        if (stickerDTO.getId() == null) {
            throw new RuntimeException("表情包ID不能为空");
        }
        validateSticker(stickerDTO);

        Sticker existing = stickerMapper.getEntityById(stickerDTO.getId());
        if (existing == null) {
            throw new RuntimeException("表情包不存在");
        }

        StickerGroupVO group = stickerGroupMapper.getVOById(stickerDTO.getGroupId());
        if (group == null) {
            throw new RuntimeException("表情包分组不存在");
        }

        Sticker sticker = Sticker.builder()
                .id(stickerDTO.getId())
                .groupId(stickerDTO.getGroupId())
                .label(stickerDTO.getLabel())
                .imageUrl(stickerDTO.getImageUrl())
                .emoji(stickerDTO.getEmoji())
                .stickerType(stickerDTO.getStickerType())
                .build();

        stickerMapper.update(sticker);
    }

    /**
     * 删除表情包
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Sticker existing = stickerMapper.getEntityById(id);
        if (existing == null) {
            throw new RuntimeException("表情包不存在");
        }
        stickerMapper.deleteById(id);
    }

    /**
     * 校验表情包类型与内容是否匹配
     */
    private void validateSticker(StickerDTO stickerDTO) {
        StickerTypeEnum typeEnum = StickerTypeEnum.fromCode(stickerDTO.getStickerType());
        if (typeEnum == null) {
            throw new RuntimeException("表情包类型不正确");
        }

        if (StickerTypeEnum.CUSTOM_IMAGE.equals(typeEnum)
                && !StringUtils.hasText(stickerDTO.getImageUrl())) {
            throw new RuntimeException("自定义图片表情包必须填写图片地址");
        }

        if (StickerTypeEnum.EMOJI.equals(typeEnum)
                && !StringUtils.hasText(stickerDTO.getEmoji())) {
            throw new RuntimeException("emoji表情包必须填写emoji");
        }
    }
}
