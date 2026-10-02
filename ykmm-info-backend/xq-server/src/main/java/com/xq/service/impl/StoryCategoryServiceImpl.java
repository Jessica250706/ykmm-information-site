package com.xq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xq.constant.StoryConstant;
import com.xq.dto.StoryCategoryDTO;
import com.xq.dto.StoryCategoryPageQueryDTO;
import com.xq.entity.StoryCategory;
import com.xq.enums.StoryCategoryTypeEnum;
import com.xq.mapper.StoryCategoryMapper;
import com.xq.result.PageResult;
import com.xq.service.StoryCategoryService;
import com.xq.vo.StoryCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    public List<StoryCategoryVO> listTree(Integer categoryType) {
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
     * 分页查询剧情分类树
     *
     * @param query 查询条件
     * @return 分类树
     */
    @Override
    public PageResult<StoryCategoryVO> pageTree(StoryCategoryPageQueryDTO query) {
        // 1. 用 PageHelper 对「根节点」分页
        PageHelper.startPage(query.getPageNum(), query.getPageSize());

        List<StoryCategory> roots;
        if (query.getParentId() != null) {
            // 分页某节点的直接子节点
            roots = storyCategoryMapper.listChildren(query.getParentId(), query.getCategoryType());
        } else {
            // 分页根节点
            roots = storyCategoryMapper.listRoots(query.getCategoryType());
        }
        PageInfo<StoryCategory> pageInfo = new PageInfo<>(roots);

        if (roots.isEmpty()) {
            return PageResult.empty(pageInfo.getTotal());
        }

        // 2. 查出这些根节点的全部子孙
        List<Long> rootIds = roots.stream().map(StoryCategory::getId).toList();
        List<StoryCategory> descendants = storyCategoryMapper.listDescendants(rootIds);

        // 3. 合并 + 转 VO
        List<StoryCategory> all = new ArrayList<>(roots.size() + descendants.size());
        all.addAll(roots);
        all.addAll(descendants);

        // 1. 转 VO，用 LinkedHashMap 也行，但关键不在这
        Map<Long, StoryCategoryVO> idMap = new HashMap<>(all.size());
        for (StoryCategory c : all) {
            StoryCategoryVO vo = toVO(c);
            vo.setChildren(new ArrayList<>());
            idMap.put(vo.getId(), vo);
        }

        // 2. 挂子节点（顺序不影响，后面会排序）
        for (StoryCategory c : all) {
            StoryCategoryVO vo = idMap.get(c.getId());
            Long pid = vo.getParentId();
            if (pid == null || pid == StoryConstant.ROOT_PARENT_ID || !idMap.containsKey(pid)) {
                continue;
            }
            idMap.get(pid).getChildren().add(vo);
        }

        // 3. 按 roots 的顺序输出根节点
        List<StoryCategoryVO> result = new ArrayList<>(roots.size());
        for (StoryCategory root : roots) {
            StoryCategoryVO vo = idMap.get(root.getId());
            if (vo != null) result.add(vo);
        }

        // 4. 对每个节点的 children 排序，保证子节点也按 categoryType, sort, id
        sortChildrenRecursively(result);

        return new PageResult<>(result, pageInfo.getTotal());
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
     * 查询分类详情（含完整子树）
     *
     * @param id 主键
     * @return 带子树的 VO
     */
    @Override
    public StoryCategoryVO getDetailTree(Long id) {
        if (id == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        StoryCategory root = storyCategoryMapper.getById(id);
        if (root == null) {
            throw new RuntimeException("剧情分类不存在");
        }

        // 查子孙（复用已有的 listDescendants）
        List<StoryCategory> descendants = storyCategoryMapper.listDescendants(List.of(id));

        List<StoryCategory> all = new ArrayList<>();
        all.add(root);
        if (descendants != null && !descendants.isEmpty()) {
            all.addAll(descendants);
        }

        // 转 VO
        Map<Long, StoryCategoryVO> idMap = new HashMap<>(all.size());
        for (StoryCategory c : all) {
            StoryCategoryVO vo = toVO(c);
            vo.setChildren(new ArrayList<>());
            idMap.put(vo.getId(), vo);
        }

        // 挂子节点
        for (StoryCategory c : all) {
            StoryCategoryVO vo = idMap.get(c.getId());
            Long pid = vo.getParentId();
            if (pid == null || pid == StoryConstant.ROOT_PARENT_ID || !idMap.containsKey(pid)) {
                continue;
            }
            idMap.get(pid).getChildren().add(vo);
        }

        StoryCategoryVO rootVo = idMap.get(id);
        sortChildrenRecursively(rootVo.getChildren());

        // 补上父分类名称
        if (root.getParentId() != null
                && !root.getParentId().equals(StoryConstant.ROOT_PARENT_ID)) {
            StoryCategory parent = storyCategoryMapper.getById(root.getParentId());
            if (parent != null) {
                rootVo.setParentName(parent.getName());
            }
        }
        return rootVo;
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

    /**
     * 排序
     */
    private void sortChildrenRecursively(List<StoryCategoryVO> list) {
        list.sort(Comparator
                .comparing(StoryCategoryVO::getCategoryType, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(StoryCategoryVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(StoryCategoryVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (StoryCategoryVO vo : list) {
            if (vo.getChildren() != null && !vo.getChildren().isEmpty()) {
                sortChildrenRecursively(vo.getChildren());
            }
        }
    }
}
