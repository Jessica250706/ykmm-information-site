import type { PageResult } from '@/types/common'
import type {
  StoryCategoryDTO,
  StoryCategoryPageQueryDTO,
  StoryCategoryQueryDTO,
  StoryCategoryVO,
} from '@/types/storyCategory'
import request from '@/utils/http'

/**
 * @description: 查询所有剧情分类树（不分页）
 */
export const listStoryCategoryTreeAPI = (params: StoryCategoryQueryDTO) => {
  return request.get<StoryCategoryVO[]>('/admin/story-category/tree', { params })
}

/**
 * @description: 分页查询剧情分类树
 */
export const pageStoryCategoryTreeAPI = (params: StoryCategoryPageQueryDTO) => {
  return request.get<PageResult<StoryCategoryVO>>('/admin/story-category/page', { params })
}

/**
 * @description: 查询分类详情
 */
export const getStoryCategoryDetailAPI = (id: number) => {
  return request.get<StoryCategoryVO>(`/admin/story-category/${id}`)
}

/**
 * @description: 新增分类
 */
export const createStoryCategoryAPI = (data: StoryCategoryDTO) => {
  return request.post('/admin/story-category', data)
}

/**
 * @description: 编辑分类
 */
export const updateStoryCategoryAPI = (id: number, data: StoryCategoryDTO) => {
  return request.put(`/admin/story-category/${id}`, data)
}

/**
 * @description: 删除分类
 */
export const deleteStoryCategoryAPI = (id: number) => {
  return request.delete(`/admin/story-category/${id}`)
}

/**
 * @description: 查询分类详情（含完整子树）
 */
export const getStoryCategoryDetailTreeAPI = (id: number) => {
  return request.get<StoryCategoryVO>(`/admin/story-category/${id}/tree`)
}
