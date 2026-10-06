import type { PageResult } from '@/types/common'
import type {
  DialogueVersionContributorDTO,
  DialogueVersionContributorPageQueryDTO,
  DialogueVersionContributorVO,
  StoryContributorVO,
} from '@/types/dialogueVersionContributor'
import request from '@/utils/http'

/**
 * @description: 分页查询贡献者
 */
export const pageDialogueVersionContributorsAPI = (
  params: DialogueVersionContributorPageQueryDTO,
) => {
  return request.get<PageResult<DialogueVersionContributorVO>>(
    '/admin/dialogue-version-contributor',
    { params },
  )
}

/**
 * @description: 查询某个版本下的贡献者列表
 */
export const listDialogueVersionContributorsByVersionAPI = (versionId: number) => {
  return request.get<DialogueVersionContributorVO[]>(
    `/admin/dialogue-version-contributor/version/${versionId}`,
  )
}

/**
 * @description: 按来源聚合查询贡献者列表（用于展示故事贡献者）
 */
export const listDialogueVersionContributorsBySourceAPI = (
  sourceType: number,
  sourceId: number,
) => {
  return request.get<StoryContributorVO[]>(
    `/admin/dialogue-version-contributor/source/${sourceType}/${sourceId}`,
  )
}

/**
 * @description: 新增贡献者
 */
export const createDialogueVersionContributorAPI = (data: DialogueVersionContributorDTO) => {
  return request.post<number>('/admin/dialogue-version-contributor', data)
}

/**
 * @description: 更新贡献者
 */
export const updateDialogueVersionContributorAPI = (
  id: number,
  data: DialogueVersionContributorDTO,
) => {
  return request.put(`/admin/dialogue-version-contributor/${id}`, data)
}

/**
 * @description: 删除单个贡献者
 */
export const deleteDialogueVersionContributorAPI = (id: number) => {
  return request.delete(`/admin/dialogue-version-contributor/${id}`)
}

/**
 * @description: 清空某个版本下的全部贡献者
 */
export const deleteDialogueVersionContributorsByVersionAPI = (versionId: number) => {
  return request.delete(`/admin/dialogue-version-contributor/version/${versionId}`)
}

/**
 * @description: 查询单个贡献者详情
 */
export const getDialogueVersionContributorDetailAPI = (id: number) => {
  return request.get<DialogueVersionContributorVO>(`/admin/dialogue-version-contributors/${id}`)
}
