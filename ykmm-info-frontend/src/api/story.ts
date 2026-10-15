import type { PageResult } from '@/types/common'
import type {
  StoryAuditDTO,
  StoryDetailVO,
  StoryDTO,
  StoryPageQueryDTO,
  StoryVO,
} from '@/types/story'
import type { StoryCategoryQueryDTO, StoryCategoryVO } from '@/types/storyCategory'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * @description: 查询剧情分类树
 */
export const listUserStoryCategoryTreeAPI = (params?: StoryCategoryQueryDTO) => {
  return request.get<StoryCategoryVO[]>('/pub/story/category/tree', { params })
}

/**
 * @description: 分页查询某分类下的剧情
 */
export const listStoryAPI = (params: StoryPageQueryDTO) => {
  return request.get<PageResult<StoryVO>>('/pub/story/page', { params })
}

/**
 * @description:  查询剧情详情（含对话）
 */
export const getStoryDetailAPI = (id: number) => {
  return request.get<StoryDetailVO>(`/pub/story/${id}`)
}

// ---------- 用户 ----------

/**
 * @description: 新增剧情
 */
export const createStoryAPI = (data: StoryDTO) => {
  return request.post('/user/story', data)
}

/**
 * @description: 编辑剧情
 */
export const updateStoryAPI = (id: number, data: StoryDTO) => {
  return request.put(`/user/story/${id}`, data)
}

// ---------- 管理员 ----------

/**
 * @description: 删除剧情
 */
export const deleteStoryAPI = (id: number) => {
  return request.delete(`/admin/story/${id}`)
}

/**
 * @description: 审核剧情
 */
export const auditStoryAPI = (id: number, data: StoryAuditDTO) => {
  return request.put(`/admin/story/${id}/audit`, data)
}

/**
 * @description: 根据分类ID查询剧情列表
 */
export const listStoryByCategoryAPI = (categoryId: number) => {
  return request.get<StoryVO[]>(`/admin/story/by-category/${categoryId}`)
}
