import type { PageResult } from '@/types/common'
import type {
  DialogueVersionDTO,
  DialogueVersionOptionVO,
  DialogueVersionPageQueryDTO,
  DialogueVersionSourceDTO,
  DialogueVersionVO,
} from '@/types/dialogueVersion'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * @description: 查询全部文字版本选项（供下拉框使用）
 */
export const listTextDialogueVersionOptionsAPI = (sourceType: number, sourceId: number) => {
  return request.get<DialogueVersionOptionVO[]>('/pub/dialogue-version/text-options', {
    params: { sourceType, sourceId },
  })
}

/**
 * @description: 查询全部图片版本选项（供下拉框使用）
 */
export const listImageDialogueVersionOptionsAPI = (sourceType: number, sourceId: number) => {
  return request.get<DialogueVersionOptionVO[]>('/pub/dialogue-version/img-options', {
    params: { sourceType, sourceId },
  })
}

/**
 * @description: 查询全部版本选项（供下拉框使用）
 */
export const listDialogueVersionOptionsAPI = (sourceType: number, sourceId: number) => {
  return request.get<DialogueVersionOptionVO[]>('/pub/dialogue-version/options', {
    params: { sourceType, sourceId },
  })
}

/**
 * @description: 查询版本详情
 */
export const getDialogueVersionDetailAPI = (id: number) => {
  return request.get<DialogueVersionVO>(`/pub/dialogue-version/${id}`)
}

// ---------- 用户 ----------

/**
 * @description: 查询某来源下的全部版本
 */
export const listDialogueVersionsBySourceAPI = (params: DialogueVersionSourceDTO) => {
  return request.get<DialogueVersionVO[]>('/user/dialogue-version/list', { params })
}

/**
 * @description: 创建版本
 */
export const createDialogueVersionAPI = (data: DialogueVersionDTO) => {
  return request.post<number>('/user/dialogue-version', data)
}

/**
 * @description: 分页查询我上传的版本
 */
export function pageMyVersionsAPI(params: DialogueVersionPageQueryDTO) {
  return request.get<PageResult<DialogueVersionVO>>('/user/dialogue-version/my', { params })
}

// ---------- 管理员 ----------

/**
 * @description: 分页查询对话版本
 */
export const pageDialogueVersionsAPI = (params: DialogueVersionPageQueryDTO) => {
  return request.get<PageResult<DialogueVersionVO>>('/admin/dialogue-version', { params })
}

/**
 * @description: 删除版本
 */
export const deleteDialogueVersionAPI = (id: number) => {
  return request.delete(`/admin/dialogue-version/${id}`)
}
