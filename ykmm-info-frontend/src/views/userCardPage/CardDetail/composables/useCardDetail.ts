import { computed, ref } from 'vue'
import { getUserCardDetailAPI } from '@/api/card'
import { CARD_ATTACHED_STORY_TYPE } from '@/constants/card'
import type { CardVO } from '@/types/card'

export function useCardDetail() {
  const loading = ref(false)
  const card = ref<CardVO | null>(null)

  /** 附属剧情类型：0无 1-RC 2-RTV 3-Rabbitter */
  const attachedType = computed(() => card.value?.attachedStoryType ?? 0)

  /** 图片 URL 列表 */
  const imageUrls = computed(() => (card.value?.images ?? []).map((i) => i.url!).filter((u) => !!u))

  const isRc = computed(() => attachedType.value === CARD_ATTACHED_STORY_TYPE.RC)
  const isRtv = computed(() => attachedType.value === CARD_ATTACHED_STORY_TYPE.RTV)

  async function load(id: number) {
    if (!id) return
    loading.value = true
    try {
      const res = await getUserCardDetailAPI(id)
      card.value = res.data ?? null
    } catch {
      card.value = null
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    card,
    attachedType,
    imageUrls,
    isRc,
    isRtv,
    load,
  }
}
