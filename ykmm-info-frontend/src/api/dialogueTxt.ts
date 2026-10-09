import type { DialogueTxtImportDTO } from '@/types/dialogueLine'
import type { DialogueTxtParseVO } from '@/types/dialogueTxt'
import request from '@/utils/http'

/**
 * @description: 解析 txt，返回预览
 */
export const parseDialogueTxtAPI = (versionId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<DialogueTxtParseVO>(`/admin/dialogue-txt/parse/${versionId}`, formData)
}

/**
 * @description: 确认导入解析结果
 */
export const importDialogueTxtAPI = (versionId: number, data: DialogueTxtImportDTO) => {
  return request.post(`/admin/dialogue-txt/import/${versionId}`, data)
}
