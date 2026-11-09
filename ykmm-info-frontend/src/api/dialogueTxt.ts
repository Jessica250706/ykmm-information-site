import type { DialogueTxtImportDTO } from '@/types/dialogueLine'
import type { DialogueTxtParseVO } from '@/types/dialogueTxt'
import request from '@/utils/http'

/** 从响应头读取 Content-Disposition 的最小接口，避免用 any */
interface HeadersLike {
  get?: (name: string) => string | null | undefined
  [key: string]: unknown
}

type BlobWithHeaders = Blob & { headers?: HeadersLike }

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
 * @param fallback  兜底文件名（后端未返回 Content-Disposition 时使用）
 */
export const exportDialogueTxtAPI = async (versionId: number, fallback?: string) => {
  const blob = (await request.get(`/user/dialogue-txt/export/${versionId}`, {
    responseType: 'blob',
  })) as unknown as BlobWithHeaders

  if (!blob?.size) throw new Error('导出文件为空')

  if (blob.type?.includes('application/json')) {
    const text = await blob.text()
    try {
      throw new Error(JSON.parse(text).msg || '导出失败')
    } catch (e) {
      throw e instanceof Error ? e : new Error(text)
    }
  }

  // 从 Content-Disposition 取文件名
  const cd = (blob.headers?.get?.('content-disposition') ?? '') as string
  const m = /filename\*=UTF-8''([^;]+)/i.exec(cd) || /filename="?([^";]+)"?/i.exec(cd)
  // ⭐ m?.[1] 保证不为 undefined，decodeURIComponent 只接受 string
  const name = m?.[1] ? decodeURIComponent(m[1]) : fallback || `dialogue-${versionId}.txt`

  const url = URL.createObjectURL(blob)
  const a = Object.assign(document.createElement('a'), { href: url, download: name })
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
