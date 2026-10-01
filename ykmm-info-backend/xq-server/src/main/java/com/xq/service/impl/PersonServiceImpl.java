package com.xq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xq.constant.PersonConstant;
import com.xq.dto.PersonDTO;
import com.xq.dto.PersonPageQueryDTO;
import com.xq.entity.Person;
import com.xq.enumeration.BloodTypeEnum;
import com.xq.enumeration.PersonTypeEnum;
import com.xq.mapper.PersonMapper;
import com.xq.result.PageResult;
import com.xq.service.PersonService;
import com.xq.vo.AgencyVO;
import com.xq.vo.IdolGroupVO;
import com.xq.vo.PersonIdolGroupVO;
import com.xq.vo.PersonVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PersonServiceImpl implements PersonService {

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------
    // 分页查询
    // ---------------------------------------------------
    @Override
    public PageResult<PersonVO> pageQuery(PersonPageQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        Page<Person> page = (Page<Person>) personMapper.pageQuery(query);

        List<PersonVO> voList = page.getResult().stream()
                .map(this::toVOWithRelations)
                .collect(Collectors.toList());

        return new PageResult(page.getTotal(), voList);
    }

    // ---------------------------------------------------
    // 详情
    // ---------------------------------------------------
    @Override
    public PersonVO getById(Long id) {
        if (id == null) {
            throw new RuntimeException("人物ID不能为空");
        }
        Person person = personMapper.getById(id);
        if (person == null) {
            throw new RuntimeException("人物不存在");
        }
        return toVOWithRelations(person);
    }

    // ---------------------------------------------------
    // 新增
    // ---------------------------------------------------
    @Override
    @Transactional
    public Long create(PersonDTO dto) {
        // 基础校验
        judgeLegality(dto);

        Person person = new Person();
        BeanUtils.copyProperties(dto, person);

        // images 转 JSON 字符串
        person.setImages(toJson(dto.getImages()));

        personMapper.insert(person);
        Long personId = person.getId();

        // 保存关系
        saveRelations(personId, dto);

        log.info("新增人物成功，id={}", personId);
        return personId;
    }

    // ---------------------------------------------------
    // 编辑
    // ---------------------------------------------------
    @Override
    @Transactional
    public void update(Long id, PersonDTO dto) {
        Person exist = personMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("人物不存在");
        }
        judgeLegality(dto);

        Person person = new Person();
        BeanUtils.copyProperties(dto, person);
        person.setId(id);
        person.setImages(toJson(dto.getImages()));

        personMapper.update(person);

        // 重建关系：先删后插
        if (dto.getGroupIds() != null) {
            personMapper.deletePersonGroupRel(id);
            if (!dto.getGroupIds().isEmpty()) {
                personMapper.insertPersonGroupRel(id, dto.getGroupIds());
            }
        }
        if (dto.getAgencyIds() != null) {
            personMapper.deletePersonAgencyRel(id);
            if (!dto.getAgencyIds().isEmpty()) {
                personMapper.insertPersonAgencyRel(id, dto.getAgencyIds());
            }
        }

        log.info("编辑人物成功，id={}", id);
    }

    /**
     * 基础校验
     *
     * @param dto
     */
    private static void judgeLegality(PersonDTO dto) {
        if (dto.getNameCn() != null && dto.getNameCn().isBlank()) {
            throw new RuntimeException("中文名不能为空");
        }
        if (dto.getPersonType() != null && !PersonTypeEnum.isValid(dto.getPersonType())) {
            throw new RuntimeException("人物类型不合法");
        }
        if (dto.getBloodType() != null && !BloodTypeEnum.isValid(dto.getBloodType())) {
            throw new RuntimeException("血型不合法");
        }
        if (dto.getSymbol() != null
                && dto.getSymbol().length() > PersonConstant.SYMBOL_MAX_LENGTH) {
            throw new RuntimeException("代表符号长度不能超过 "
                    + PersonConstant.SYMBOL_MAX_LENGTH);
        }
    }

    // ---------------------------------------------------
    // 删除
    // ---------------------------------------------------
    @Override
    @Transactional
    public void delete(Long id) {
        Person exist = personMapper.getById(id);
        if (exist == null) {
            throw new RuntimeException("人物不存在");
        }
        // 被卡面引用时不允许删除
        int refCount = personMapper.countCardRefByPersonId(id);
        if (refCount > 0) {
            throw new RuntimeException("该人物已被卡面引用，无法删除");
        }
        // 删除关系
        personMapper.deletePersonGroupRel(id);
        personMapper.deletePersonAgencyRel(id);
        // 删除人物
        personMapper.deleteById(id);
        log.info("删除人物成功，id={}", id);
    }

    // ---------------------------------------------------
    // 私有方法
    // ---------------------------------------------------

    /**
     * 保存人物-团体/公司关系
     */
    private void saveRelations(Long personId, PersonDTO dto) {
        if (dto.getGroupIds() != null && !dto.getGroupIds().isEmpty()) {
            personMapper.insertPersonGroupRel(personId, dto.getGroupIds());
        }
        if (dto.getAgencyIds() != null && !dto.getAgencyIds().isEmpty()) {
            personMapper.insertPersonAgencyRel(personId, dto.getAgencyIds());
        }
    }

    /**
     * Person -> PersonVO，包含关系
     */
    private PersonVO toVOWithRelations(Person person) {
        PersonVO vo = new PersonVO();
        BeanUtils.copyProperties(person, vo);

        vo.setPersonTypeLabel(PersonTypeEnum.getLabel(person.getPersonType()));
        vo.setBloodTypeLabel(BloodTypeEnum.getLabel(person.getBloodType()));
        vo.setImages(fromJson(person.getImages()));

        // 团体
        List<PersonIdolGroupVO> groups = personMapper.listGroupsByPersonId(person.getId());
        if (groups != null) {
            vo.setGroups(groups.stream()
                    .map(g -> new PersonIdolGroupVO(g.getId(), g.getName()))
                    .collect(Collectors.toList()));
        } else {
            vo.setGroups(Collections.emptyList());
        }

        // 公司
        List<AgencyVO> agencies = personMapper.listAgenciesByPersonId(person.getId());
        if (agencies != null) {
            vo.setAgencies(agencies.stream()
                    .map(a -> new AgencyVO(a.getId(), a.getName()))
                    .collect(Collectors.toList()));
        } else {
            vo.setAgencies(Collections.emptyList());
        }

        return vo;
    }

    /**
     * List<String> -> JSON 字符串
     */
    private String toJson(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("序列化 images 失败", e);
            return null;
        }
    }

    /**
     * JSON 字符串 -> List<String>
     */
    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("反序列化 images 失败：{}", json, e);
            return new ArrayList<>();
        }
    }
}
