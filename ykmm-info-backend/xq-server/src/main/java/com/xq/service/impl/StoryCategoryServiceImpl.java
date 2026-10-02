package com.xq.service.impl;

import com.xq.constant.StoryConstant;
import com.xq.dto.StoryCategoryDTO;
import com.xq.entity.StoryCategory;
import com.xq.enums.StoryCategoryTypeEnum;
import com.xq.mapper.StoryCategoryMapper;
import com.xq.service.StoryCategoryService;
import com.xq.vo.StoryCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剧情分类服务实现
 */
@Service
@Slf4j
public class StoryCategoryServiceImpl implements StoryCategoryService {

    @Autowired
    private StoryCategoryMapper storyCategoryMapper;

    /**
     * 查询剧情分类树
     *
     * @param categoryType 分类类型，null 返回全部
     * @return 分类树
     */
    @Override
    public List<StoryCategoryVO> tree(Integer categoryType) {
        List<StoryCategory> all;
        if (categoryType == null) {
            all = storyCategoryMapper.listAll();
        } else {
            all = storyCategoryMapper.listByType(categoryType);
        }

        List<StoryCategoryVO> voList = new ArrayList<>();
        if (all == null || all.isEmpty()) {
            return voList;
        }

        // 先转 VO
        Map<Long, StoryCategoryVO> idMap = new HashMap<>();
        for (StoryCategory c : all) {
            StoryCategoryVO vo = toVO(c);
            vo.setChildren(new ArrayList<>());
            idMap.put(vo.getId(), vo);
            voList.add(vo);
        }

        // 构建树
        List<StoryCategoryVO> roots = new ArrayList<>();
        for (StoryCategoryVO vo : voList) {
            Long pid = vo.getParentId();
            if (pid == null || pid == StoryConstant.ROOT_PARENT_ID
                    || !idMap.containsKey(pid)) {
                roots.add(vo);
            } else {
                idMap.get(pid).getChildren().add(vo);
            }
        }
        return roots;
    }

    /**
     * 查询分类详情
     *
     * @param id 主键
     * @return 分类详情
     */
    @Override
    public StoryCategoryVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        StoryCategory category = storyCategoryMapper.getById(id);
        if (category == null) {
            throw new RuntimeException("剧情分类不存在");
        }
        return toVO(category);
    }

    /**
     * 新增分类
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @Override
    @Transactional
    public Long create(StoryCategoryDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("分类名不能为空");
        }
        if (!StoryCategoryTypeEnum.isValid(dto.getCategoryType())) {
            throw new RuntimeException("分类类型不合法");
        }
        // 父分类处理
        if (dto.getParentId() == null) {
            dto.setParentId(StoryConstant.ROOT_PARENT_ID);
        } else if (dto.getParentId() != StoryConstant.ROOT_PARENT_ID) {
            StoryCategory parent = storyCategoryMapper.getById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在");
            }
        }

        StoryCategory category = new StoryCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        storyCategoryMapper.insert(category);
        log.info("新增剧情分类成功，id={}", category.getId());
        return category.getId();
    }

    /**
     * 编辑分类
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, StoryCategoryDTO dto) {
        if (id == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        StoryCategory exist = storyCategoryMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情分类不存在");
        }
        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new RuntimeException("分类名不能为空");
        }
        if (dto.getCategoryType() != null
                && !StoryCategoryTypeEnum.isValid(dto.getCategoryType())) {
            throw new RuntimeException("分类类型不合法");
        }
        // 不能把自己设为父分类
        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new RuntimeException("父分类不能是自己");
        }
        // 父分类存在性校验
        if (dto.getParentId() != null
                && dto.getParentId() != StoryConstant.ROOT_PARENT_ID) {
            StoryCategory parent = storyCategoryMapper.getById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在");
            }
        }

        StoryCategory category = new StoryCategory();
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        storyCategoryMapper.update(category);
        log.info("编辑剧情分类成功，id={}", id);
    }

    /**
     * 删除分类
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        StoryCategory exist = storyCategoryMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("剧情分类不存在");
        }
        // 有子分类不允许删除
        int childCount = storyCategoryMapper.countChildren(id);
        if (childCount > 0) {
            throw new RuntimeException("请先删除子分类");
        }
        // 有剧情引用不允许删除
        int storyCount = storyCategoryMapper.countStoryByCategoryId(id);
        if (storyCount > 0) {
            throw new RuntimeException("该分类下还有剧情，无法删除");
        }
        storyCategoryMapper.deleteById(id);
        log.info("删除剧情分类成功，id={}", id);
    }

    /**
     * StoryCategory -> StoryCategoryVO
     *
     * @param category 实体
     * @return VO
     */
    private StoryCategoryVO toVO(StoryCategory category) {
        if (category == null) {
            return null;
        }
        StoryCategoryVO vo = new StoryCategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setCategoryTypeLabel(
                StoryCategoryTypeEnum.getLabel(category.getCategoryType()));
        return vo;
    }
}
