package com.xq.service.impl;

import com.xq.dto.NoticeCategoryDTO;
import com.xq.entity.SysNoticeCategory;
import com.xq.mapper.NoticeCategoryMapper;
import com.xq.mapper.NoticeMapper;
import com.xq.service.NoticeCategoryService;
import com.xq.vo.NoticeCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 公告分类服务实现
 */
@Service
@Slf4j
public class NoticeCategoryServiceImpl implements NoticeCategoryService {

    @Autowired
    private NoticeCategoryMapper noticeCategoryMapper;
    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public List<NoticeCategoryVO> listAll() {
        List<SysNoticeCategory> list = noticeCategoryMapper.listAll();
        if (list == null || list.isEmpty()) return Collections.emptyList();

        List<NoticeCategoryVO> result = new ArrayList<>(list.size());
        for (SysNoticeCategory c : list) {
            NoticeCategoryVO vo = new NoticeCategoryVO();
            BeanUtils.copyProperties(c, vo);
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public void create(NoticeCategoryDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("分类ID不能为空");
        }
        if (noticeCategoryMapper.getById(dto.getId()) != null) {
            throw new RuntimeException("分类ID已存在：" + dto.getId());
        }

        SysNoticeCategory category = new SysNoticeCategory();
        BeanUtils.copyProperties(dto, category);
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);

        noticeCategoryMapper.insert(category);
        log.info("新增公告分类成功，id={}, name={}", category.getId(), category.getName());
    }

    @Override
    @Transactional
    public void update(Integer id, NoticeCategoryDTO dto) {
        if (id == null) throw new RuntimeException("分类ID不能为空");
        if (dto == null) throw new RuntimeException("参数不能为空");

        SysNoticeCategory exist = noticeCategoryMapper.getById(id);
        if (exist == null) throw new RuntimeException("分类不存在：" + id);

        SysNoticeCategory category = new SysNoticeCategory();
        category.setId(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setColor(dto.getColor());
        category.setSort(dto.getSort());

        noticeCategoryMapper.update(category);
        log.info("编辑公告分类成功，id={}", id);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (id == null) throw new RuntimeException("分类ID不能为空");

        SysNoticeCategory exist = noticeCategoryMapper.getById(id);
        if (exist == null) throw new RuntimeException("分类不存在：" + id);

        // 分类下仍有公告时不允许删除，避免公告变成"孤儿"
        int count = noticeMapper.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("该分类下还有 " + count + " 条公告，无法删除");
        }

        noticeCategoryMapper.deleteById(id);
        log.info("删除公告分类成功，id={}", id);
    }
}
