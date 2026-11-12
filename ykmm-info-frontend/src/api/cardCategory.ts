import type { PageResult } from '@/components/ProTable'
import type {
  CardCategoryDTO,
  CardCategoryPageQueryDTO,
  CardCategoryVO,
} from '@/types/cardCategory'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * 分页查询类别
 */
export function pageCardCategoryAPI(params: CardCategoryPageQueryDTO) {
  return request.get<PageResult<CardCategoryVO>>('/pub/card-category/page', { params })
}

/**
 * 全部类别（下拉用，按 sort 升序）
 */
export function listCardCategoryOptionsAPI() {
  return request.get<CardCategoryVO[]>('/pub/card-category/list')
}

/**
 * 类别详情
 */
export function getCardCategoryDetailAPI(id: number) {
  return request.get<CardCategoryVO>(`/pub/card-category/${id}`)
}

// ---------- 管理员 ----------

/**
 * 新增（仅管理员）
 */
export function createCardCategoryAPI(data: CardCategoryDTO) {
  return request.post<number>('/admin/card-category', data)
}

/**
 * 编辑（仅管理员）
 */
export function updateCardCategoryAPI(id: number, data: CardCategoryDTO) {
  return request.put<void>(`/admin/card-category/${id}`, data)
}

/**
 * 删除（仅管理员）
 */
export function deleteCardCategoryAPI(id: number) {
  return request.delete<void>(`/admin/card-category/${id}`)
}
