package com.xq.service.impl;

import com.xq.dto.StoryCategoryTypeDTO;
import com.xq.entity.StoryCategoryType;
import com.xq.mapper.StoryCategoryTypeMapper;
import com.xq.service.StoryCategoryTypeService;
import com.xq.vo.StoryCategoryTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 剧情分类类型实现
 */
@Service
@Slf4j
public class StoryCategoryTypeServiceImpl implements StoryCategoryTypeService {

    @Autowired
    private StoryCategoryTypeMapper storyCategoryTypeMapper;

    /**
     * 查询全部
     *
     * @return 列表
     */
    @Override
    public List<StoryCategoryTypeVO> listAll() {
        List<StoryCategoryType> list = storyCategoryTypeMapper.listAll();
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 详情
     */
    @Override
    public StoryCategoryTypeVO getById(Integer id) {
        if (id == null) {
            throw new RuntimeException("分类类型ID不能为空");
        }
        StoryCategoryType type = storyCategoryTypeMapper.getById(id);
        if (type == null) {
            throw new RuntimeException("分类类型不存在");
        }
        return toVO(type);
    }

    /**
     * 编辑
     *
     * @param id  主键
     * @param dto 编辑参数
     */
    @Override
    @Transactional
    public void update(Integer id, StoryCategoryTypeDTO dto) {
        if (id == null) {
            throw new RuntimeException("分类类型ID不能为空");
        }
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }
        StoryCategoryType exist = storyCategoryTypeMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("分类类型不存在");
        }
        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new RuntimeException("名称不能为空");
        }

        StoryCategoryType type = new StoryCategoryType();
        BeanUtils.copyProperties(dto, type);
        type.setId(id);
        storyCategoryTypeMapper.update(type);
        log.info("编辑剧情分类类型成功，id={}", id);
    }

    /**
     * Entity -> VO
     *
     * @param type 实体
     * @return VO
     */
    private StoryCategoryTypeVO toVO(StoryCategoryType type) {
        if (type == null) {
            return null;
        }
        StoryCategoryTypeVO vo = new StoryCategoryTypeVO();
        BeanUtils.copyProperties(type, vo);
        return vo;
    }
}
