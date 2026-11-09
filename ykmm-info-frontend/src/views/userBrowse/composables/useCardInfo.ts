import { computed, type MaybeRefOrGetter, ref, toValue } from 'vue'
import { getCardDetailAPI } from '@/api/card'
import type { CardVO } from '@/types/card'

/** 属性 → 主题色映射 */
const ATTR_COLOR_MAP: Record<number, string> = {
  1: 'red',
  2: 'lime',
  3: 'blue',
}

/**
 * 卡面基本信息：加载详情、计算名称与主题色。
 *
 * @param cardId 卡面ID（支持 ref / getter，卡面切换时会重新加载）
 */
export function useCardInfo(cardId: MaybeRefOrGetter<number>) {
  const color = ref('blue')
  const cardInfo = ref<CardVO>({})
  const cardName = computed(
    () => `${cardInfo.value.name ?? ''}[${cardInfo.value.seriesName ?? ''}]`,
  )

  /**
   * 加载卡面详情。
   * <p>
   * 静默失败：拿不到数据就保持默认值（蓝色主题 + 空名称），
   * 不阻塞主流程，也不弹错误提示——外层还有话数加载更重要。
   */
  async function loadCardInfo() {
    const id = toValue(cardId)
    if (!id) return
    try {
      const res = await getCardDetailAPI(id)
      cardInfo.value = res.data ?? {}
      color.value = cardInfo.value.attribute
        ? (ATTR_COLOR_MAP[cardInfo.value.attribute] ?? 'blue')
        : 'blue'
    } catch {
      // 保持默认值
    }
  }

  return { color, cardInfo, cardName, loadCardInfo }
}
