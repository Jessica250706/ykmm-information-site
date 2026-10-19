import type {
  CardDTO,
  CardEpisodeDialogueDTO,
  CardEpisodeVO,
  CardPageQueryDTO,
  CardReviewDTO,
  CardVO,
} from '@/types/card'
import type { PageResult } from '@/types/common'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * @description: 分页查询卡面
 */
export const pageCardAPI = (params: CardPageQueryDTO) => {
  return request.get<PageResult<CardVO>>('/pub/card/page', { params })
}

/**
 * @description: 查询卡面详情
 */
export const getCardDetailAPI = (id: number) => {
  return request.get<CardVO>(`/pub/card/${id}`)
}

/**
 * @description: 查询某一话的对话
 */
export const getCardEpisodeDialogueAPI = (params: CardEpisodeDialogueDTO) => {
  return request.get<CardEpisodeVO>(`/pub/card/episode/dialogue`, { params })
}

/**
 * @description: 判断当前用户能否新增 RC、RTV、Rabitter
 */
export const canCreateAPI = (cardId: number) => {
  return request.get<boolean>(`/pub/card/can-create/${cardId}`)
}

// ---------- 用户 ----------

/**
 * @description: 新增卡面
 */
export const createCardAPI = (data: CardDTO) => {
  return request.post<number>('/user/card', data)
}

/**
 * @description: 编辑卡面
 */
export const updateCardAPI = (id: number, data: CardDTO) => {
  return request.put(`/user/card/${id}`, data)
}

// ---------- 管理员 ----------

/**
 * @description: 删除卡面
 */
export const deleteCardAPI = (id: number) => {
  return request.delete(`/admin/card/${id}`)
}

/**
 * @description: 审核卡面
 */
export function reviewCardAPI(id: number, data: CardReviewDTO) {
  return request.post(`/admin/card/${id}/review`, data)
}
