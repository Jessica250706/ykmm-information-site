import type { PageResult } from '@/types/common'
import type {
  StickerGroupDTO,
  StickerGroupPageQueryDTO,
  StickerGroupVO,
  StickerGroupWithStickersVO,
} from '@/types/sticker'
import request from '@/utils/http'

// ---------- 用户 ----------

/**
 * @description: 查询所有表情包分组（下拉用）
 */
export const listStickerGroupOptionsAPI = () => {
  return request.get<StickerGroupVO[]>('/user/sticker-group/list')
}

/**
 * @description: 查询所有分组及下属表情包（表情选择器用）
 */
export const listStickerGroupWithStickersAPI = () => {
  return request.get<StickerGroupWithStickersVO[]>('/user/sticker-group/with-stickers')
}

// ---------- 管理员 ----------

/**
 * @description: 分页查询表情包分组
 */
export const listStickerGroupAPI = (params: StickerGroupPageQueryDTO) => {
  return request.get<PageResult<StickerGroupVO>>('/admin/sticker-group/page', { params })
}

/**
 * @description: 查询表情包分组详情
 */
export const getStickerGroupDetailAPI = (id: number) => {
  return request.get<StickerGroupVO>(`/admin/sticker-group/${id}`)
}

/**
 * @description: 新增表情包分组
 */
export const createStickerGroupAPI = (data: StickerGroupDTO) => {
  return request.post('/admin/sticker-group', data)
}

/**
 * @description: 编辑表情包分组
 */
export const updateStickerGroupAPI = (id: number, data: StickerGroupDTO) => {
  return request.put(`/admin/sticker-group/${id}`, data)
}

/**
 * @description: 删除表情包分组
 */
export const deleteStickerGroupAPI = (id: number) => {
  return request.delete(`/admin/sticker-group/${id}`)
}
