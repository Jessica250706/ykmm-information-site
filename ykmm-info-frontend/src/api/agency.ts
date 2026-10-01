import type { AgencyDTO, AgencyVO } from '@/types/agency'
import request from '@/utils/http'

/**
 * @description: 查询全部经纪公司
 */
export const listAgencyAPI = () => {
  return request.get<AgencyVO[]>('/admin/agencies')
}

/**
 * @description: 查询经纪公司详情
 */
export const getAgencyDetailAPI = (id: number) => {
  return request.get<AgencyVO>(`/admin/agencies/${id}`)
}

/**
 * @description: 新增经纪公司
 */
export const createAgencyAPI = (data: AgencyDTO) => {
  return request.post('/admin/agencies', data)
}

/**
 * @description: 编辑经纪公司
 */
export const updateAgencyAPI = (id: number, data: AgencyDTO) => {
  return request.put(`/admin/agencies/${id}`, data)
}

/**
 * @description: 删除经纪公司
 */
export const deleteAgencyAPI = (id: number) => {
  return request.delete(`/admin/agencies/${id}`)
}
