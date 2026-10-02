import type { PageResult } from '@/types/common'
import type {
  StoryCategoryDTO,
  StoryCategoryPageQueryDTO,
  StoryCategoryVO,
} from '@/types/storyCategory'
import request from '@/utils/http'

/**
 * @description: 查询剧情分类树
 */
export const listStoryCategoryAPI = (params: StoryCategoryPageQueryDTO) => {
  return request.get<StoryCategoryVO[]>('/admin/story-category', { params })
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
