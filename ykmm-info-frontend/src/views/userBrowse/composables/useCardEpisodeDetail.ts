import { ref } from 'vue'
import { getCardEpisodeDialogueAPI } from '@/api/card'
import { listDialogueVersionOptionsAPI } from '@/api/dialogueVersion'
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
      episodeDetail.value = res.data
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
