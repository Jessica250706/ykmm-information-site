import type { PageResult } from '@/types/common'
import type { RoleDTO, RoleGroupVO, RolePageQueryDTO, RoleVO } from '@/types/role'
import request from '@/utils/http'

/**
 * @description: 分页查询角色
 */
export const listRoleAPI = (params: RolePageQueryDTO) => {
  return request.get<PageResult<RoleVO>>('/admin/role', { params })
}

/**
 * @description: 查询全部角色（按人物分组）
 */
export const listGroupedRolesAPI = () => {
  return request.get<RoleGroupVO[]>('/admin/role/all-grouped')
}

/**
 * @description: 查询角色详情
 */
export const getRoleDetailAPI = (id: number) => {
  return request.get<RoleVO>(`/admin/role/${id}`)
}

/**
 * @description: 新增角色
 */
export const createRoleAPI = (data: RoleDTO) => {
  return request.post('/admin/role', data)
}

/**
 * @description: 编辑角色
 */
export const updateRoleAPI = (id: number, data: RoleDTO) => {
  return request.put(`/admin/role/${id}`, data)
}

/**
 * @description: 删除角色
 */
export const deleteRoleAPI = (id: number) => {
  return request.delete(`/admin/role/${id}`)
}
