import type { UserRequest, UserInfo, UserEditDTO, UserStatusDTO } from '@/types/user'
import type { PageResult } from '@/types/common'
import request from '@/utils/http'

/**
 * @description: 用户列表（分页 + 条件查询）
 */
export const listUserAPI = (params: UserRequest) => {
  return request.get<PageResult<UserInfo>>('/admin/users', { params })
}

/**
 * @description: 用户详情
 */
export const detailUserAPI = (id: number) => {
  return request.get<UserInfo>(`/admin/users/${id}`)
}

/**
 * @description: 编辑用户
 */
export const editUserAPI = (id: number, params: UserEditDTO) => {
  return request.put(`/admin/users/${id}`, { params })
}

/**
 * @description: 删除用户
 */
export const deleteUserAPI = (id: number) => {
  return request.delete(`/admin/users/${id}`)
}

/**
 * @description: 启用/禁用用户
 */
export const updateUserStatusAPI = (id: number, params: UserStatusDTO) => {
  return request.put(`/admin/users/${id}/status`, { params })
}
