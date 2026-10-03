import type { DialogueImageDTO, DialogueLineSortDTO } from '@/types/dialogueImage'
import request from '@/utils/http'

/**
 * @description: 批量保存图片列表
 */
export const batchSaveDialogueImagesAPI = (versionId: number, data: DialogueImageDTO[]) => {
  return request.post(`/admin/dialogue-images/batch/${versionId}`, data)
}

/**
 * @description: 删除单张图片
 */
export const deleteDialogueImageAPI = (id: number) => {
  return request.delete(`/admin/dialogue-images/${id}`)
}

/**
 * @description: 调整图片顺序
 */
export const sortDialogueImagesAPI = (versionId: number, data: DialogueLineSortDTO) => {
  return request.put(`/admin/dialogue-images/sort/${versionId}`, data)
}
