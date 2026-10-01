import type { IdolGroupDTO, IdolGroupVO } from '@/types/idolGroup'
import request from '@/utils/http'

/**
 * @description: 查询全部偶像团体
 */
export const listIdolGroupsAPI = () => {
  return request.get<IdolGroupVO[]>('/admin/idol-groups')
}

/**
 * @description: 查询偶像团体详情
 */
export const getIdolGroupDetailAPI = (id: number) => {
  return request.get<IdolGroupVO>(`/admin/idol-groups/${id}`)
}

/**
 * @description: 新增偶像团体
 */
export const createIdolGroupAPI = (data: IdolGroupDTO) => {
  return request.post('/admin/idol-groups', data)
}

/**
 * @description: 编辑偶像团体
 */
export const updateIdolGroupAPI = (id: number, data: IdolGroupDTO) => {
  return request.put(`/admin/idol-groups/${id}`, data)
}

/**
 * @description: 删除偶像团体
 */
export const deleteIdolGroupAPI = (id: number) => {
  return request.delete(`/admin/idol-groups/${id}`)
}
