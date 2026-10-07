import type { CardSeriesDTO, CardSeriesPageQueryDTO, CardSeriesVO } from '@/types/cardSeries'
import type { PageResult } from '@/types/common'
import request from '@/utils/http'

/**
 * @description: 分页查询卡面系列
 */
export const pageCardSeriesAPI = (params: CardSeriesPageQueryDTO) => {
  return request.get<PageResult<CardSeriesVO>>('/admin/card-series', { params })
}

/**
 * @description: 查询全部卡面系列（下拉用）
 */
export const listCardSeriesOptionsAPI = () => {
  return request.get<CardSeriesVO[]>('/admin/card-series/options')
}

/**
 * @description: 按名称搜索卡面系列（下拉远程搜索用）
 */
export const searchCardSeriesAPI = (keyword?: string) => {
  return request.get<CardSeriesVO[]>('/admin/card-series/search', { params: { keyword } })
}

/**
 * @description: 查询卡面系列详情
 */
export const getCardSeriesDetailAPI = (id: number) => {
  return request.get<CardSeriesVO>(`/admin/card-series/${id}`)
}

/**
 * @description: 新增卡面系列
 */
export const createCardSeriesAPI = (data: CardSeriesDTO) => {
  return request.post<number>('/admin/card-series', data)
}

/**
 * @description: 编辑卡面系列
 */
export const updateCardSeriesAPI = (id: number, data: CardSeriesDTO) => {
  return request.put(`/admin/card-series/${id}`, data)
}

/**
 * @description: 删除卡面系列
 */
export const deleteCardSeriesAPI = (id: number) => {
  return request.delete(`/admin/card-series/${id}`)
}

/**
 * @description: 按名称查询或创建卡面系列（下拉"输入不存在则新增"场景）
 */
export const findOrCreateCardSeriesAPI = (name: string) => {
  return request.post<number>('/admin/card-series/find-or-create', null, {
    params: { name },
  })
}
