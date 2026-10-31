import type { PageResult } from '@/types/common'
import type { NoticeCategoryVO, NoticeDTO, NoticePageQueryDTO, NoticeVO } from '@/types/notice'
import request from '@/utils/http'

/* -------- 公共端 -------- */

export function pageVisibleNoticesAPI(params: NoticePageQueryDTO) {
  return request.get<PageResult<NoticeVO>>('/pub/notice/page', { params })
}

export function getVisibleNoticeDetailAPI(id: number) {
  return request.get<NoticeVO>(`/pub/notice/${id}`)
}

export function listNoticeCategoriesAPI() {
  return request.get<NoticeCategoryVO[]>('/pub/notice-category/list')
}

/* -------- 管理端 -------- */

export function pageAdminNoticesAPI(params: NoticePageQueryDTO) {
  return request.get<PageResult<NoticeVO>>('/admin/notice/page', { params })
}

export function getAdminNoticeDetailAPI(id: number) {
  return request.get<NoticeVO>(`/admin/notice/${id}`)
}

export function createNoticeAPI(data: NoticeDTO) {
  return request.post<number>('/admin/notice', data)
}

export function updateNoticeAPI(id: number, data: NoticeDTO) {
  return request.put<void>(`/admin/notice/${id}`, data)
}

export function deleteNoticeAPI(id: number) {
  return request.delete<void>(`/admin/notice/${id}`)
}

export function publishNoticeAPI(id: number) {
  return request.put<void>(`/admin/notice/${id}/publish`)
}

export function offlineNoticeAPI(id: number) {
  return request.put<void>(`/admin/notice/${id}/offline`)
}

/* -------- 分类管理 -------- */

export function adminListNoticeCategoriesAPI() {
  return request.get<NoticeCategoryVO[]>('/admin/notice-category/list')
}

export function createNoticeCategoryAPI(data: NoticeCategoryVO) {
  return request.post<void>('/admin/notice-category', data)
}

export function updateNoticeCategoryAPI(id: number, data: NoticeCategoryVO) {
  return request.put<void>(`/admin/notice-category/${id}`, data)
}

export function deleteNoticeCategoryAPI(id: number) {
  return request.delete<void>(`/admin/notice-category/${id}`)
}
