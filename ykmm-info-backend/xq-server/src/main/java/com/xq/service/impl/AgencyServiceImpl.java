package com.xq.service.impl;

import com.xq.dto.AgencyDTO;
import com.xq.entity.Agency;
import com.xq.mapper.AgencyMapper;
import com.xq.service.AgencyService;
import com.xq.vo.AgencyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 经纪公司服务实现
 */
@Service
@Slf4j
public class AgencyServiceImpl implements AgencyService {

    @Autowired
    private AgencyMapper agencyMapper;

    /**
     * 查询全部经纪公司
     *
     * @return 经纪公司列表
     */
    @Override
    public List<AgencyVO> listAll() {
        List<Agency> list = agencyMapper.listAll();
        List<AgencyVO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (Agency agency : list) {
            result.add(toVO(agency));
        }
        return result;
    }

    /**
     * 根据 id 查询详情
     *
     * @param id 主键
     * @return 经纪公司详情
     */
    @Override
    public AgencyVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("公司ID不能为空");
        }
        Agency agency = agencyMapper.getById(id);
        if (agency == null) {
            throw new RuntimeException("经纪公司不存在");
        }
        return toVO(agency);
    }

    /**
     * 新增经纪公司
     *
     * @param dto 新增参数
     * @return 新增后的 id
     */
    @Override
    @Transactional
    public Long create(AgencyDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("公司名不能为空");
        }
        // 唯一性校验
        Agency exist = agencyMapper.getByName(dto.getName());
        if (exist != null) {
            throw new RuntimeException("公司名已存在");
        }
        Agency agency = new Agency();
        BeanUtils.copyProperties(dto, agency);
        agencyMapper.insert(agency);
        log.info("新增经纪公司成功，id={}", agency.getId());
        return agency.getId();
    }

    /**
     * 编辑经纪公司
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, AgencyDTO dto) {
        if (id == null) {
            throw new RuntimeException("公司ID不能为空");
        }
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("公司名不能为空");
        }
        Agency exist = agencyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("经纪公司不存在");
        }
        // 名称变更时做唯一性校验
        if (!dto.getName().equals(exist.getName())) {
            Agency byName = agencyMapper.getByName(dto.getName());
            if (byName != null && !byName.getId().equals(id)) {
                throw new RuntimeException("公司名已存在");
            }
        }
        Agency agency = new Agency();
        BeanUtils.copyProperties(dto, agency);
        agency.setId(id);
        agencyMapper.update(agency);
        log.info("编辑经纪公司成功，id={}", id);
    }

    /**
     * 删除经纪公司
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("公司ID不能为空");
        }
        Agency exist = agencyMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("经纪公司不存在");
        }
        // 有团体引用时不允许删除
        int groupCount = agencyMapper.countGroupByAgencyId(id);
        if (groupCount > 0) {
            throw new RuntimeException("该公司下还有偶像团体，无法删除");
        }
        // 有人物引用时不允许删除
        int personCount = agencyMapper.countPersonByAgencyId(id);
        if (personCount > 0) {
            throw new RuntimeException("该公司已被人物引用，无法删除");
        }
        agencyMapper.deleteById(id);
        log.info("删除经纪公司成功，id={}", id);
    }

    /**
     * Agency -> AgencyVO
     *
     * @param agency 实体
     * @return VO
     */
    private AgencyVO toVO(Agency agency) {
        if (agency == null) {
            return null;
        }
        AgencyVO vo = new AgencyVO();
        BeanUtils.copyProperties(agency, vo);
        return vo;
    }
}
