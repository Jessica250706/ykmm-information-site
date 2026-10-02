import type { PageResult } from '@/types/common'
import type { PersonDTO, PersonPageQueryDTO, PersonVO } from '@/types/person'
import request from '@/utils/http'

/**
 * @description: 分页查询人物
 */
export const listPersonAPI = (params: PersonPageQueryDTO) => {
  return request.get<PageResult<PersonVO>>('/admin/person', { params })
}

/**
 * @description: 查询人物详情
 */
export const getPersonDetailAPI = (id: number) => {
  return request.get<PersonVO>(`/admin/person/${id}`)
}

/**
 * @description: 新增人物
 */
export const createPersonAPI = (data: PersonDTO) => {
  return request.post('/admin/person', data)
}

/**
 * @description: 编辑人物
 */
export const updatePersonAPI = (id: number, data: PersonDTO) => {
  return request.put(`/admin/person/${id}`, data)
}

/**
 * @description: 删除人物
 */
export const deletePersonAPI = (id: number) => {
  return request.delete(`/admin/person/${id}`)
}
