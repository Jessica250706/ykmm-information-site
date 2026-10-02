package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.dto.RoleDTO;
import com.xq.dto.RolePageQueryDTO;
import com.xq.dto.StoryCategorySimpleDTO;
import com.xq.entity.Person;
import com.xq.entity.Role;
import com.xq.entity.StoryCategory;
import com.xq.mapper.PersonMapper;
import com.xq.mapper.RoleMapper;
import com.xq.mapper.StoryCategoryMapper;
import com.xq.result.PageResult;
import com.xq.service.RoleService;
import com.xq.vo.RoleVO;
import com.xq.vo.StoryCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色服务实现
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private StoryCategoryMapper storyCategoryMapper;

    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<RoleVO> pageQuery(RolePageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<Role> page = (Page<Role>) roleMapper.pageQuery(query);

        List<RoleVO> voList = new ArrayList<>();
        for (Role role : page.getResult()) {
            voList.add(toVO(role));
        }
        return new PageResult(page.getTotal(), voList);
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 角色详情
     */
    @Override
    public RoleVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("角色ID不能为空");
        }
        Role role = roleMapper.getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        return toVO(role);
    }

    /**
     * 新增角色
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @Override
    @Transactional
    public Long create(RoleDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("角色名称不能为空");
        }
        // personId 可以为空，非空时校验人物存在
        if (dto.getPersonId() != null) {
            Person person = personMapper.getById(dto.getPersonId());
            if (person == null) {
                throw new RuntimeException("对应人物不存在");
            }
        }
        // 校验分类根节点是否存在
        List<Long> categoryIds = dto.getStoryCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long cid : categoryIds) {
                StoryCategory category = storyCategoryMapper.getById(cid);
                if (category == null) {
                    throw new RuntimeException("剧情分类不存在：id=" + cid);
                }
            }
        }

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        roleMapper.insert(role);
        Long roleId = role.getId();

        // 保存角色-分类关系
        if (categoryIds != null && !categoryIds.isEmpty()) {
            roleMapper.insertCategoryRel(roleId, categoryIds);
        }

        log.info("新增角色成功，id={}", roleId);
        return roleId;
    }

    /**
     * 编辑角色
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, RoleDTO dto) {
        if (id == null) {
            throw new RuntimeException("角色ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        Role exist = roleMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("角色不存在");
        }
        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new RuntimeException("角色名称不能为空");
        }
        // 人物存在性校验
        if (dto.getPersonId() != null) {
            Person person = personMapper.getById(dto.getPersonId());
            if (person == null) {
                throw new RuntimeException("对应人物不存在");
            }
        }
        // 分类存在性校验
        List<Long> categoryIds = dto.getStoryCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long cid : categoryIds) {
                StoryCategory category = storyCategoryMapper.getById(cid);
                if (category == null) {
                    throw new RuntimeException("剧情分类不存在：id=" + cid);
                }
            }
        }

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        role.setId(id);
        roleMapper.update(role);

        // 重建分类关系：先删后插
        if (categoryIds != null) {
            roleMapper.deleteCategoryRel(id);
            if (!categoryIds.isEmpty()) {
                roleMapper.insertCategoryRel(id, categoryIds);
            }
        }

        log.info("编辑角色成功，id={}", id);
    }

    /**
     * 删除角色
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("角色ID不能为空");
        }
        Role exist = roleMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("角色不存在");
        }
        // 删除分类关系
        roleMapper.deleteCategoryRel(id);
        // 删除角色
        roleMapper.deleteById(id);
        log.info("删除角色成功，id={}", id);
    }

    /**
     * Role -> RoleVO
     *
     * @param role 实体
     * @return VO
     */
    private RoleVO toVO(Role role) {
        if (role == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 对应人物中文名
        if (role.getPersonId() != null) {
            Person person = personMapper.getById(role.getPersonId());
            if (person != null) {
                vo.setPersonNameCn(person.getNameCn());
            }
        }

        // 分类根节点列表
        List<StoryCategorySimpleDTO> categories =
                roleMapper.listCategoriesByRoleId(role.getId());
        List<StoryCategoryVO> categoryVOs = new ArrayList<>();
        if (categories != null) {
            for (StoryCategorySimpleDTO c : categories) {
                StoryCategoryVO cVO = new StoryCategoryVO();
                cVO.setId(c.getId());
                cVO.setName(c.getName());
                cVO.setCategoryType(c.getCategoryType());
                categoryVOs.add(cVO);
            }
        }
        vo.setStoryCategories(categoryVOs);

        return vo;
    }
}
