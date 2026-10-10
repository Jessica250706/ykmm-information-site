import type { PageResult } from '@/types/common'
import type {
  DialogueVersionDTO,
  DialogueVersionOptionVO,
  DialogueVersionPageQueryDTO,
  DialogueVersionSourceDTO,
  DialogueVersionVO,
} from '@/types/dialogueVersion'
import request from '@/utils/http'

/**
 * @description: 分页查询对话版本
 */
export const pageDialogueVersionsAPI = (params: DialogueVersionPageQueryDTO) => {
  return request.get<PageResult<DialogueVersionVO>>('/admin/dialogue-versions', { params })
}

/**
 * @description: 查询某来源下的全部版本
 */
export const listDialogueVersionsBySourceAPI = (params: DialogueVersionSourceDTO) => {
  return request.get<DialogueVersionVO[]>('/admin/dialogue-versions/list', { params })
}

/**
 * @description: 查询版本详情
 */
export const getDialogueVersionDetailAPI = (id: number) => {
  return request.get<DialogueVersionVO>(`/admin/dialogue-versions/${id}`)
}

/**
 * @description: 创建版本
 */
export const createDialogueVersionAPI = (data: DialogueVersionDTO) => {
  return request.post<number>('/admin/dialogue-versions', data)
}

/**
 * @description: 删除版本
 */
export const deleteDialogueVersionAPI = (id: number) => {
  return request.delete(`/admin/dialogue-versions/${id}`)
}

/**
 * @description: 查询全部文字版本选项（供下拉框使用）
 */
export const listTextDialogueVersionOptionsAPI = (sourceType: number, sourceId: number) => {
  return request.get<DialogueVersionOptionVO[]>('/admin/dialogue-versions/text-options', {
    params: { sourceType, sourceId },
  })
}

/**
 * @description: 查询全部图片版本选项（供下拉框使用）
 */
export const listImageDialogueVersionOptionsAPI = (sourceType: number, sourceId: number) => {
  return request.get<DialogueVersionOptionVO[]>('/admin/dialogue-versions/img-options', {
    params: { sourceType, sourceId },
  })
}

/**
 * @description: 查询全部版本选项（供下拉框使用）
 */
export const listDialogueVersionOptionsAPI = (sourceType: number, sourceId: number) => {
  return request.get<DialogueVersionOptionVO[]>('/admin/dialogue-versions/options', {
    params: { sourceType, sourceId },
  })
}
