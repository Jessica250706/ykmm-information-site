import request from '@/utils/http'

/**
 * @description: 上传图片/文件，返回访问 URL
 */
export const uploadFileAPI = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/common/upload', formData)
}
