import { computed, onMounted, ref } from 'vue'
import { canCreateAPI, getCardDetailAPI } from '@/api/card'
import { SOURCE_TYPE } from '@/constants'
import type { CardVO } from '@/types/card'

export function useCardDetail(cardId: number) {
  const loading = ref(false)
  const card = ref<CardVO | null>(null)

  /** 附属剧情类型：0无 1-RC 2-RTV 3-Rabitter */
  const attachedType = computed(() => card.value?.attachedStoryType ?? 0)

  /** 图片 URL 列表 */
  const imageUrls = computed(() => (card.value?.images ?? []).map((i) => i.url!).filter((u) => !!u))

  const isRc = computed(() => attachedType.value === SOURCE_TYPE.RC)
  const isRtv = computed(() => attachedType.value === SOURCE_TYPE.RTV)
  const isRabitter = computed(() => attachedType.value === SOURCE_TYPE.RABITTER)

  const canOperate = ref<boolean>(false)

  async function load(id: number) {
    if (!id) return
    loading.value = true
    try {
      const res = await getCardDetailAPI(id)
      card.value = res.data ?? null
    } catch {
      card.value = null
    } finally {
      loading.value = false
    }
  }

  async function canCreate() {
    const res = await canCreateAPI(cardId)
    canOperate.value = res.data
  }

  onMounted(() => {
    canCreate()
  })

  return {
    loading,
    card,
    attachedType,
    imageUrls,
    isRc,
    isRtv,
    isRabitter,
    canOperate,
    load,
  }
}
