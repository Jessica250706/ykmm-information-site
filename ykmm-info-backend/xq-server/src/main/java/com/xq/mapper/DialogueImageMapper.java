package com.xq.mapper;

import com.xq.entity.DialogueImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话图片 Mapper
 */
@Mapper
public interface DialogueImageMapper {

    /**
     * 按版本查询图片
     *
     * @param versionId 版本ID
     * @return 图片列表
     */
    List<DialogueImage> listByVersionId(@Param("versionId") Long versionId);

    /**
     * 根据 id 查询
     *
     * @param id 主键
     * @return 图片
     */
    DialogueImage getById(@Param("id") Long id);

    /**
     * 批量新增
     *
     * @param images 图片列表
     * @return 影响行数
     */
    int insertBatch(@Param("images") List<DialogueImage> images);

    /**
     * 更新排序
     *
     * @param id   图片ID
     * @param sort 排序
     * @return 影响行数
     */
    int updateSort(@Param("id") Long id, @Param("sort") Integer sort);

    /**
     * 删除
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按版本删除全部图片
     *
     * @param versionId 版本ID
     * @return 影响行数
     */
    int deleteByVersionId(@Param("versionId") Long versionId);
}
