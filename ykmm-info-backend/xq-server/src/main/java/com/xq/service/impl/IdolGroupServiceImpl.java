package com.xq.service.impl;

import com.xq.dto.IdolGroupDTO;
import com.xq.entity.Agency;
import com.xq.entity.IdolGroup;
import com.xq.mapper.AgencyMapper;
import com.xq.mapper.IdolGroupMapper;
import com.xq.service.IdolGroupService;
import com.xq.vo.IdolGroupVO;
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
 * 偶像团体服务实现
 */
@Service
@Slf4j
public class IdolGroupServiceImpl implements IdolGroupService {

    @Autowired
    private IdolGroupMapper idolGroupMapper;

    @Autowired
    private AgencyMapper agencyMapper;

    /**
     * 查询全部偶像团体
     *
     * @return 偶像团体列表
     */
    @Override
    public List<IdolGroupVO> listAll() {
        List<IdolGroup> list = idolGroupMapper.listAll();
        List<IdolGroupVO> result = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return result;
        }
        // 一次性查全部公司，避免循环查询
        Map<Long, String> agencyNameMap = new HashMap<>();
        List<Agency> agencies = agencyMapper.listAll();
        if (agencies != null) {
            for (Agency a : agencies) {
                agencyNameMap.put(a.getId(), a.getName());
            }
        }
        for (IdolGroup g : list) {
            result.add(toVO(g, agencyNameMap));
        }
        return result;
    }

    /**
     * 根据 id 查询详情
     *
     * @param id 主键
     * @return 偶像团体详情
     */
    @Override
    public IdolGroupVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("团体ID不能为空");
        }
        IdolGroup group = idolGroupMapper.getById(id);
        if (group == null) {
            throw new RuntimeException("偶像团体不存在");
        }
        String agencyName = null;
        if (group.getAgencyId() != null) {
            Agency agency = agencyMapper.getById(group.getAgencyId());
            if (agency != null) {
                agencyName = agency.getName();
            }
        }
        IdolGroupVO vo = new IdolGroupVO();
        BeanUtils.copyProperties(group, vo);
        vo.setAgencyName(agencyName);
        return vo;
    }

    /**
     * 新增偶像团体
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @Override
    @Transactional
    public Long create(IdolGroupDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("团体名不能为空");
        }
        if (dto.getAgencyId() == null) {
            throw new RuntimeException("所属经纪公司不能为空");
        }
        // 公司存在性校验
        Agency agency = agencyMapper.getById(dto.getAgencyId());
        if (agency == null) {
            throw new RuntimeException("所属经纪公司不存在");
        }
        // 团体名唯一性校验
        IdolGroup exist = idolGroupMapper.getByName(dto.getName());
        if (exist != null) {
            throw new RuntimeException("团体名已存在");
        }

        IdolGroup group = new IdolGroup();
        BeanUtils.copyProperties(dto, group);
        idolGroupMapper.insert(group);
        log.info("新增偶像团体成功，id={}", group.getId());
        return group.getId();
    }

    /**
     * 编辑偶像团体
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, IdolGroupDTO dto) {
        if (id == null) {
            throw new RuntimeException("团体ID不能为空");
        }
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("团体名不能为空");
        }
        IdolGroup exist = idolGroupMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("偶像团体不存在");
        }
        // 名称变更时做唯一性校验
        if (!dto.getName().equals(exist.getName())) {
            IdolGroup byName = idolGroupMapper.getByName(dto.getName());
            if (byName != null && !byName.getId().equals(id)) {
                throw new RuntimeException("团体名已存在");
            }
        }
        // 公司存在性校验
        if (dto.getAgencyId() != null) {
            Agency agency = agencyMapper.getById(dto.getAgencyId());
            if (agency == null) {
                throw new RuntimeException("所属经纪公司不存在");
            }
        }

        IdolGroup group = new IdolGroup();
        BeanUtils.copyProperties(dto, group);
        group.setId(id);
        idolGroupMapper.update(group);
        log.info("编辑偶像团体成功，id={}", id);
    }

    /**
     * 删除偶像团体
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("团体ID不能为空");
        }
        IdolGroup exist = idolGroupMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("偶像团体不存在");
        }
        // 有人物引用时不允许删除
        int personCount = idolGroupMapper.countPersonByGroupId(id);
        if (personCount > 0) {
            throw new RuntimeException("该团体下还有人物，无法删除");
        }
        idolGroupMapper.deleteById(id);
        log.info("删除偶像团体成功，id={}", id);
    }

    /**
     * IdolGroup -> IdolGroupVO
     *
     * @param group         实体
     * @param agencyNameMap 公司名映射
     * @return VO
     */
    private IdolGroupVO toVO(IdolGroup group, Map<Long, String> agencyNameMap) {
        if (group == null) {
            return null;
        }
        IdolGroupVO vo = new IdolGroupVO();
        BeanUtils.copyProperties(group, vo);
        if (group.getAgencyId() != null && agencyNameMap != null) {
            vo.setAgencyName(agencyNameMap.get(group.getAgencyId()));
        }
        return vo;
    }
}
