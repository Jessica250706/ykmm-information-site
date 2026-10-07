import type { CardRcDTO, CardRcVO } from '@/types/cardRc'
import request from '@/utils/http'

/**
 * @description: 查询卡面下的 RC 列表
 */
export const listCardRcByCardAPI = (cardId: number) => {
  return request.get<CardRcVO[]>(`/admin/card-rc/card/${cardId}`)
}

/**
 * @description: 查询 RC 详情
 */
export const getCardRcDetailAPI = (id: number) => {
  return request.get<CardRcVO>(`/admin/card-rc/${id}`)
}

/**
 * @description: 新增 RC
 */
export const createCardRcAPI = (data: CardRcDTO) => {
  return request.post<number>('/admin/card-rc', data)
}

/**
 * @description: 编辑 RC
 */
export const updateCardRcAPI = (id: number, data: CardRcDTO) => {
  return request.put(`/admin/card-rc/${id}`, data)
}

/**
 * @description: 删除 RC
 */
export const deleteCardRcAPI = (id: number) => {
  return request.delete(`/admin/card-rc/${id}`)
}
