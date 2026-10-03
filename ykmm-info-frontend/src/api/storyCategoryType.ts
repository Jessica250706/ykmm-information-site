import type { StoryCategoryTypeDTO, StoryCategoryTypeVO } from '@/types/storyCategoryType'
import request from '@/utils/http'

/**
 * @description: 查询全部
 */
export const listStoryCategoryTypeAPI = () => {
  return request.get<StoryCategoryTypeVO[]>('/admin/story-category-type/list')
}

/**
 * @description: 查询详情
 */
export const getStoryCategoryTypeDetailAPI = (id: number) => {
  return request.get<StoryCategoryTypeVO>(`/admin/story-category-type/${id}`)
}

/**
 * @description: 编辑分类
 */
export const updateStoryCategoryTypeAPI = (id: number, data: StoryCategoryTypeDTO) => {
  return request.put(`/admin/story-category-type/${id}`, data)
}
