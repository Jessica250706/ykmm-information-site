import { ref } from 'vue'
import { getCardEpisodeDialogueAPI } from '@/api/card'
import { listDialogueVersionOptionsAPI } from '@/api/dialogueVersion'
import { SOURCE_TYPE, STATUS } from '@/constants'
import type { CardEpisodeVO } from '@/types/card'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'

/**
 * 卡面话数对话详情 composable
 *
 * 复用 StoryDetailVO 形状，使 BrowseCenter / BrowseRight /
 * useDialogueEdit / useVersionSelection 无需区分来源。
 */
export function useCardEpisodeDetail() {
  /**
   * 当前话数详情
   * 形状对齐 StoryDetailVO：至少包含 id / title / versions
   */
  const episodeDetail = ref<StoryDetailVO | null>(null)

  /** 拉取中 */
  const loading = ref(false)

  /** 该话可用的版本选项（语言 / 格式 / 范围组合） */
  const versionOptions = ref<DialogueVersionOptionVO[] | null>(null)

  /**
   * 清空当前话数数据
   */
  function reset() {
    episodeDetail.value = null
    versionOptions.value = null
  }

  /**
   * 拉取某一话对话详情
   *
   * @param sourceType 来源类型：1-RC 2-RTV 3-Rabitter
   * @param sourceId   来源ID
   */
  async function fetchEpisodeDetail(sourceType: number, sourceId: number) {
    loading.value = true
    try {
      const res = await getCardEpisodeDialogueAPI({ sourceType, sourceId })
      const data = res.data as CardEpisodeVO | null

      if (!data) {
        episodeDetail.value = null
        return
      }

      /**
       * 适配为 StoryDetailVO 形状
       * - id：话数ID，useDialogueEdit 用它做 key
       * - title：话标题
       * - versions：对话版本（含 lines / images / options / contributors）
       * - categoryId / categoryType：卡面语境下无意义，留空
       */
      episodeDetail.value = {
        id: data.id,
        title: data.title,
        versions: data.versions ?? [],
      } as unknown as StoryDetailVO
    } finally {
      loading.value = false
    }
  }

  /**
   * 拉取该话的版本选项
   */
  async function fetchVersionOptions(sourceType: number, sourceId: number) {
    const res = await listDialogueVersionOptionsAPI(sourceType, sourceId)
    versionOptions.value = res.data ?? []
    versionOptions.value = null
  }

  return {
    episodeDetail,
    loading,
    versionOptions,
    reset,
    fetchEpisodeDetail,
    fetchVersionOptions,
  }
}
