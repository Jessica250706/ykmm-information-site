import type { PageResult } from '@/types/common'
import type { SysMessagePageQueryDTO, SysMessageVO } from '@/types/message'
import request from '@/utils/http'

// ---------- 用户端 ----------

/**
 * @description: 分页查询我的消息
 */
export const pageMyMessagesAPI = (params: SysMessagePageQueryDTO) => {
  return request.get<PageResult<SysMessageVO>>('/user/message/page', { params })
}

/**
 * @description: 查询我的消息详情
 */
export const getMyMessageDetailAPI = (id: number) => {
  return request.get<SysMessageVO>(`/user/message/${id}`)
}

/**
 * @description: 我的未读消息数
 */
export const getUnreadCountAPI = () => {
  return request.get<number>('/user/message/unread-count')
}

/**
 * @description: 标记单条消息为已读
 */
export const markReadAPI = (id: number) => {
  return request.put<void>(`/user/message/${id}/read`)
}

/**
 * @description: 标记全部消息为已读
 */
export const markAllReadAPI = () => {
  return request.put<void>('/user/message/read-all')
}

// ---------- 管理端 ----------

/**
 * @description: 管理端分页查询消息（可按用户筛选）
 */
export const pageMessagesAPI = (params: SysMessagePageQueryDTO) => {
  return request.get<PageResult<SysMessageVO>>('/admin/message/page', { params })
}

/**
 * @description: 管理端查询消息详情
 */
export const getMessageDetailAPI = (id: number) => {
  return request.get<SysMessageVO>(`/admin/message/${id}`)
}
