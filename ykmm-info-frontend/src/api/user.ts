import type { PageResult } from '@/types/common'
import type {
  RebindEmailDTO,
  SendRebindEmailCodeDTO,
  UserEditDTO,
  UserInfo,
  UserRequest,
  UserStatusDTO,
  VerifyRebindOldCodeDTO,
} from '@/types/user'
import request from '@/utils/http'

// ---------- 用户 ----------

/**
 * @description: 用户详情
 */
export const detailUserAPI = (id: number) => {
  return request.get<UserInfo>(`/user/user/${id}`)
}

/**
 * @description: 编辑用户
 */
export const editUserAPI = (id: number, data: UserEditDTO) => {
  return request.put<UserInfo>(`/user/user/${id}`, data)
}

/**
 * @description: 发送换绑邮箱验证码
 */
export function sendRebindEmailCodeAPI(data: SendRebindEmailCodeDTO) {
  return request.post<void>('/user/user/email/rebind/send-code', data)
}

/**
 * @description: 换绑邮箱
 */
export function rebindEmailAPI(data: RebindEmailDTO) {
  return request.put<UserInfo>('/user/user/email/rebind', data)
}

/**
 * @description: 校验旧邮箱验证码（换绑邮箱 Step 1）
 */
export function verifyOldEmailCodeAPI(data: VerifyRebindOldCodeDTO) {
  return request.post<void>('/user/user/email/rebind/verify-old', data)
}

// ---------- 管理员 ----------

/**
 * @description: 用户列表（分页 + 条件查询）
 */
export const listUserAPI = (params: UserRequest) => {
  return request.get<PageResult<UserInfo>>('/admin/user', { params })
}

/**
 * @description: 删除用户
 */
export const deleteUserAPI = (id: number) => {
  return request.delete(`/admin/user/${id}`)
}

/**
 * @description: 启用/禁用用户
 */
export const updateUserStatusAPI = (id: number, params: UserStatusDTO) => {
  return request.put(`/admin/user/${id}/status`, { params })
}
