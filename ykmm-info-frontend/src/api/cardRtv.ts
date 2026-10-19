import type { CardRtvDTO, CardRtvVO } from '@/types/cardRtv'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * @description: 查询卡面下的 RTV 列表
 */
export const listCardRtvByCardAPI = (cardId: number) => {
  return request.get<CardRtvVO[]>(`/pub/card-rtv/card/${cardId}`)
}

/**
 * @description: 查询 RTV 详情
 */
export const getCardRtvDetailAPI = (id: number) => {
  return request.get<CardRtvVO>(`/pub/card-rtv/${id}`)
}

// ---------- 用户 ----------

/**
 * @description: 新增 RTV
 */
export const createCardRtvAPI = (data: CardRtvDTO) => {
  return request.post<number>('/user/card-rtv', data)
}

/**
 * @description: 编辑 RTV
 */
export const updateCardRtvAPI = (id: number, data: CardRtvDTO) => {
  return request.put(`/user/card-rtv/${id}`, data)
}

// ---------- 管理员 ----------

/**
 * @description: 删除 RTV
 */
export const deleteCardRtvAPI = (id: number) => {
  return request.delete(`/admin/card-rtv/${id}`)
}
