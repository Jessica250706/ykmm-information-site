import { computed, ref, watch } from 'vue'

export type CardSize = 'small' | 'normal' | 'big'

const CARD_SIZE_STORAGE_KEY = 'user-card-size'

/**
 * 卡片尺寸偏好：仅本地状态，不进 URL、不影响数据请求
 */
export function useCardSize() {
  const cardSize = ref<CardSize>(
    (localStorage.getItem(CARD_SIZE_STORAGE_KEY) as CardSize) || 'normal',
  )

  watch(cardSize, (v) => {
    localStorage.setItem(CARD_SIZE_STORAGE_KEY, v)
  })

  const gridClass = computed(() => {
    switch (cardSize.value) {
      case 'small':
        return 'grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 xl:grid-cols-10'
      case 'big':
        return 'grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4'
      case 'normal':
      default:
        return 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5'
    }
  })

  return { cardSize, gridClass }
}
