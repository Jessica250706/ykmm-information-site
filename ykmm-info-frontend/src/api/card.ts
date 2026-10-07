import type { CardDTO, CardPageQueryDTO, CardVO } from '@/types/card'
import type { PageResult } from '@/types/common'
import request from '@/utils/http'

/**
 * @description: 分页查询卡面
 */
export const pageCardAPI = (params: CardPageQueryDTO) => {
  return request.get<PageResult<CardVO>>('/admin/card', { params })
}

/**
 * @description: 查询卡面详情
 */
export const getCardDetailAPI = (id: number) => {
  return request.get<CardVO>(`/admin/card/${id}`)
}

/**
 * @description: 新增卡面
 */
export const createCardAPI = (data: CardDTO) => {
  return request.post<number>('/admin/card', data)
}

/**
 * @description: 编辑卡面
 */
export const updateCardAPI = (id: number, data: CardDTO) => {
  return request.put(`/admin/card/${id}`, data)
}

/**
 * @description: 删除卡面
 */
export const deleteCardAPI = (id: number) => {
  return request.delete(`/admin/card/${id}`)
}
