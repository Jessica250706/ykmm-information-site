import type { DialogueLineDTO, DialogueLineSortDTO } from '@/types/dialogueLine'
import request from '@/utils/http'

/**
 * @description: 批量保存句子
 */
export const batchSaveDialogueLinesAPI = (versionId: number, data: DialogueLineDTO[]) => {
  return request.post(`/admin/dialogue-line/batch/${versionId}`, data)
}

/**
 * @description: 编辑单句
 */
export const updateDialogueLineAPI = (id: number, data: DialogueLineDTO) => {
  return request.put(`/admin/dialogue-line/${id}`, data)
}

/**
 * @description: 删除单句
 */
export const deleteDialogueLineAPI = (id: number) => {
  return request.delete(`/admin/dialogue-line/${id}`)
}

/**
 * @description: 调整句子顺序
 */
export const sortDialogueLinesAPI = (versionId: number, data: DialogueLineSortDTO) => {
  return request.put(`/admin/dialogue-line/sort/${versionId}`, data)
}

/**
 * 批量更新对话句子
 */
export const updateDialogueLinesBatchAPI = (versionId: number, lines: DialogueLineDTO[]) => {
  return request.put(`/admin/dialogue-line/batch/${versionId}`, lines)
}

/**
 * 批量删除对话句子
 */
export const deleteDialogueLinesBatchAPI = (lineIds: number[]) => {
  return request.delete('/admin/dialogue-line/batch', { data: lineIds })
}
