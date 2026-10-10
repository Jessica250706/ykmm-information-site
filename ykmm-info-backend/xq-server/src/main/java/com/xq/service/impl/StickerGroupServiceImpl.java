package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.context.BaseContext;
import com.xq.dto.StickerGroupDTO;
import com.xq.dto.StickerGroupPageQueryDTO;
import com.xq.entity.StickerGroup;
import com.xq.mapper.StickerGroupMapper;
import com.xq.mapper.StickerMapper;
import com.xq.result.PageResult;
import com.xq.service.StickerGroupService;
import com.xq.vo.StickerGroupVO;
import com.xq.vo.StickerGroupWithStickersVO;
import com.xq.vo.StickerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表情包分组Service实现
 */
@Service
@RequiredArgsConstructor
public class StickerGroupServiceImpl implements StickerGroupService {

    private final StickerGroupMapper stickerGroupMapper;
    private final StickerMapper stickerMapper;

    /**
     * 分页查询表情包分组
     */
    @Override
    public PageResult<StickerGroupVO> pageQuery(StickerGroupPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<StickerGroupVO> page = stickerGroupMapper.pageQuery(queryDTO);

        return new PageResult<>(page.getTotal(), page);
    }

    /**
     * 查询所有表情包分组
     */
    @Override
    public List<StickerGroupVO> listAll() {
        return stickerGroupMapper.listAll();
    }

    /**
     * 根据ID查询表情包分组
     */
    @Override
    public StickerGroupVO getById(Long id) {
        StickerGroupVO stickerGroupVO = stickerGroupMapper.getVOById(id);
        if (stickerGroupVO == null) {
            throw new RuntimeException("表情包分组不存在");
        }
        return stickerGroupVO;
    }

    /**
     * 新增表情包分组
     */
    @Override
    @Transactional
    public void save(StickerGroupDTO stickerGroupDTO) {
        long count = stickerGroupMapper.countByName(stickerGroupDTO.getName(), null);
        if (count > 0) {
            throw new RuntimeException("分组名称已存在");
        }

        StickerGroup stickerGroup = StickerGroup.builder()
                .name(stickerGroupDTO.getName())
                .description(stickerGroupDTO.getDescription())
                .sort(stickerGroupDTO.getSort() == null ? 0 : stickerGroupDTO.getSort())
                .creatorId(BaseContext.getCurrentId())
                .build();

        stickerGroupMapper.insert(stickerGroup);
    }

    /**
     * 更新表情包分组
     */
    @Override
    @Transactional
    public void update(StickerGroupDTO stickerGroupDTO) {
        if (stickerGroupDTO.getId() == null) {
            throw new RuntimeException("分组ID不能为空");
        }

        StickerGroup existing = stickerGroupMapper.getEntityById(stickerGroupDTO.getId());
        if (existing == null) {
            throw new RuntimeException("表情包分组不存在");
        }

        long count = stickerGroupMapper.countByName(stickerGroupDTO.getName(), stickerGroupDTO.getId());
        if (count > 0) {
            throw new RuntimeException("分组名称已存在");
        }

        StickerGroup stickerGroup = StickerGroup.builder()
                .id(stickerGroupDTO.getId())
                .name(stickerGroupDTO.getName())
                .description(stickerGroupDTO.getDescription())
                .sort(stickerGroupDTO.getSort())
                .build();

        stickerGroupMapper.update(stickerGroup);
    }

    /**
     * 删除表情包分组
     */
    @Override
    @Transactional
    public void delete(Long id) {
        StickerGroup existing = stickerGroupMapper.getEntityById(id);
        if (existing == null) {
            throw new RuntimeException("表情包分组不存在");
        }

        long stickerCount = stickerMapper.countByGroupId(id);
        if (stickerCount > 0) {
            throw new RuntimeException("该分组下还有表情包，不能删除");
        }

        stickerGroupMapper.deleteById(id);
    }

    /**
     * 查询所有分组及其表情包（用于表情选择器）
     */
    @Override
    public List<StickerGroupWithStickersVO> listWithStickers() {
        List<StickerGroupVO> groups = stickerGroupMapper.listAll();
        if (groups.isEmpty()) return new ArrayList<>();

        List<Long> groupIds = groups.stream()
                .map(StickerGroupVO::getId)
                .filter(Objects::nonNull)
                .toList();

        List<StickerVO> all = stickerMapper.listByGroupIds(groupIds);
        Map<Long, List<StickerVO>> stickerMap = all.stream()
                .filter(s -> s.getGroupId() != null)
                .collect(Collectors.groupingBy(StickerVO::getGroupId));

        return groups.stream().map(g -> {
            StickerGroupWithStickersVO vo = new StickerGroupWithStickersVO();
            vo.setId(g.getId());
            vo.setName(g.getName());
            vo.setDescription(g.getDescription());
            vo.setSort(g.getSort());
            vo.setStickers(stickerMap.getOrDefault(g.getId(), new ArrayList<>()));
            return vo;
        }).toList();
    }
}
