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

/**
 * @description: 分页查询剧情
 */
export const listStoryAPI = (params: StoryPageQueryDTO) => {
  return request.get<PageResult<StoryVO>>('/admin/story', { params })
}

/**
 * @description: 查询剧情详情
 */
export const getStoryDetailAPI = (id: number) => {
  return request.get<StoryVO>(`/admin/story/${id}`)
}

/**
 * @description: 新增剧情
 */
export const createStoryAPI = (data: StoryDTO) => {
  return request.post('/admin/story', data)
}

/**
 * @description: 编辑剧情
 */
export const updateStoryAPI = (id: number, data: StoryDTO) => {
  return request.put(`/admin/story/${id}`, data)
}

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
 * @description: 用户端 - 查询剧情分类树
 */
export const listUserStoryCategoryTreeAPI = (params?: StoryCategoryQueryDTO) => {
  return request.get<StoryCategoryVO[]>('/user/story/category/tree', { params })
}

/**
 * @description: 用户端 - 分页查询某分类下的剧情
 */
export const pageUserStoryAPI = (params: StoryPageQueryDTO) => {
  return request.get<PageResult<StoryVO>>('/user/story/page', { params })
}

/**
 * @description: 用户端 - 查询剧情详情（含对话）
 */
export const getUserStoryDetailAPI = (id: number) => {
  return request.get<StoryDetailVO>(`/user/story/${id}`)
}
