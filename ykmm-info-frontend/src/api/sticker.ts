import type { PageResult } from '@/types/common'
import type { StickerDTO, StickerPageQueryDTO, StickerVO } from '@/types/sticker'
import request from '@/utils/http'

/**
 * @description: 分页查询表情包
 */
export const listStickerAPI = (params: StickerPageQueryDTO) => {
  return request.get<PageResult<StickerVO>>('/admin/sticker/page', { params })
}

/**
 * @description: 查询表情包详情
 */
export const getStickerDetailAPI = (id: number) => {
  return request.get<StickerVO>(`/admin/sticker/${id}`)
}

/**
 * @description: 新增表情包
 */
export const createStickerAPI = (data: StickerDTO) => {
  return request.post('/admin/sticker', data)
}

/**
 * @description: 编辑表情包
 */
export const updateStickerAPI = (id: number, data: StickerDTO) => {
  return request.put(`/admin/sticker/${id}`, data)
}

/**
 * @description: 删除表情包
 */
export const deleteStickerAPI = (id: number) => {
  return request.delete(`/admin/sticker/${id}`)
}
