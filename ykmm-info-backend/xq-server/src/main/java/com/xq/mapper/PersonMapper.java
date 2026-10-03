package com.xq.mapper;

import com.xq.dto.PersonPageQueryDTO;
import com.xq.entity.Person;
import com.xq.vo.AgencyVO;
import com.xq.vo.IdolGroupVO;
import com.xq.vo.PersonIdolGroupVO;
import com.xq.vo.PersonOptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PersonMapper {

    /**
     * 分页查询
     */
    List<Person> pageQuery(@Param("query") PersonPageQueryDTO query);

    /**
     * 查询全部人物
     */
    List<PersonOptionVO> listOptions();

    /**
     * 统计总数
     */
    long countByQuery(@Param("query") PersonPageQueryDTO query);

    /**
     * 根据 id 查询
     */
    Person getById(@Param("id") Long id);

    /**
     * 新增
     */
    int insert(Person person);

    /**
     * 更新
     */
    int update(Person person);

    /**
     * 删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 查询人物-团体关系
     */
    List<Long> listGroupIdsByPersonId(@Param("personId") Long personId);

    /**
     * 查询人物-公司关系
     */
    List<Long> listAgencyIdsByPersonId(@Param("personId") Long personId);

    /**
     * 删除人物-团体关系
     */
    int deletePersonGroupRel(@Param("personId") Long personId);

    /**
     * 删除人物-公司关系
     */
    int deletePersonAgencyRel(@Param("personId") Long personId);

    /**
     * 批量插入人物-团体关系
     */
    int insertPersonGroupRel(@Param("personId") Long personId,
                             @Param("groupIds") List<Long> groupIds);

    /**
     * 批量插入人物-公司关系
     */
    int insertPersonAgencyRel(@Param("personId") Long personId,
                              @Param("agencyIds") List<Long> agencyIds);

    /**
     * 查询团体信息
     */
    List<PersonIdolGroupVO> listGroupsByPersonId(@Param("personId") Long personId);

    /**
     * 查询公司信息
     */
    List<AgencyVO> listAgenciesByPersonId(@Param("personId") Long personId);

    /**
     * 判断人物是否被卡面引用
     */
    int countCardRefByPersonId(@Param("personId") Long personId);

    /**
     * 按 ID 集合批量查询人物
     *
     * @param ids 主键集合
     * @return 人物列表
     */
    List<Person> listByIds(@Param("ids") Collection<Long> ids);
}
