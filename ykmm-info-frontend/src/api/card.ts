import type {
  CardDTO,
  CardEpisodeDialogueDTO,
  CardEpisodeVO,
  CardPageQueryDTO,
  CardVO,
} from '@/types/card'
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

/**
 * @description: 用户端 - 分页查询已发布卡面
 */
export const pageUserCardAPI = (params: CardPageQueryDTO) => {
  return request.get<PageResult<CardVO>>('/user/card/page', { params })
}

/**
 * @description: 用户端 - 查询卡面详情
 */
export const getUserCardDetailAPI = (id: number) => {
  return request.get<CardVO>(`/user/card/${id}`)
}

/**
 * @description: 用户端 - 查询某一话的对话
 */
export const getCardEpisodeDialogueAPI = (params: CardEpisodeDialogueDTO) => {
  return request.get<CardEpisodeVO>(`/user/card/episode/dialogue`, { params })
}
