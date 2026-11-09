import type { DialogueTxtImportDTO } from '@/types/dialogueLine'
import type { DialogueTxtParseVO } from '@/types/dialogueTxt'
import request from '@/utils/http'

/**
 * @description: 解析 txt，返回预览
 */
export const parseDialogueTxtAPI = (versionId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<DialogueTxtParseVO>(`/user/dialogue-txt/parse/${versionId}`, formData)
}

/**
 * @description: 确认导入解析结果
 */
export const importDialogueTxtAPI = (versionId: number, data: DialogueTxtImportDTO) => {
  return request.post(`/user/dialogue-txt/import/${versionId}`, data)
}

/**
 * @description: 导出为 txt 文本（走浏览器下载）
 * @param versionId 版本ID
 * @param filename  下载文件名（可选，默认由后端 Content-Disposition 决定）
 */
export const exportDialogueTxtAPI = async (versionId: number, filename?: string) => {
  const blob = (await request.get(`/user/dialogue-txt/export/${versionId}`, {
    responseType: 'blob',
  })) as unknown as Blob

  // 兜底 1：空文件
  if (!blob || blob.size === 0) {
    throw new Error('导出文件为空')
  }

  // 兜底 2：万一拦截器没识别出 JSON 错误（例如后端错误也用 text/plain 返回）
  const contentType = blob.type || ''
  if (contentType.includes('application/json')) {
    const text = await blob.text()
    try {
      const json = JSON.parse(text)
      throw new Error(json.msg || json.message || '导出失败')
    } catch (err) {
      if (err instanceof Error) throw err
      throw new Error(text || '导出失败')
    }
  }

  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || `dialogue-${versionId}.txt`
  document.body.appendChild(a)
  a.click()
  a.remove()
  // 延迟 revoke，避免部分浏览器（Safari/Firefox）下载被中断
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
