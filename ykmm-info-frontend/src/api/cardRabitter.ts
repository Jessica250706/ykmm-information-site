import type { CardRabitterDTO, CardRabitterVO } from '@/types/cardRabitter'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * @description: 查询卡面下的 Rabitter 列表
 */
export const listCardRabitterByCardAPI = (cardId: number) => {
  return request.get<CardRabitterVO[]>(`/pub/card-rabitter/card/${cardId}`)
}

/**
 * @description: 查询 Rabitter 详情
 */
export const getCardRabitterDetailAPI = (id: number) => {
  return request.get<CardRabitterVO>(`/pub/card-rabitter/${id}`)
}

// ---------- 用户 ----------

/**
 * @description: 新增 Rabitter
 */
export const createCardRabitterAPI = (data: CardRabitterDTO) => {
  return request.post<number>('/user/card-rabitter', data)
}

/**
 * @description: 编辑 Rabitter
 */
export const updateCardRabitterAPI = (id: number, data: CardRabitterDTO) => {
  return request.put(`/user/card-rabitter/${id}`, data)
}

// ---------- 管理员 ----------

/**
 * @description: 删除 Rabitter
 */
export const deleteCardRabitterAPI = (id: number) => {
  return request.delete(`/admin/card-rabitter/${id}`)
}
