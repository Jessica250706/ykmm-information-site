import {
  CARD_ATTACHED_STORY_TYPE,
  CARD_ATTRIBUTE_TAG_TYPE,
  CARD_IMAGE_TYPE,
  CARD_MAX_RARITY,
  type CardAttributeValue,
} from '@/constants'
import type { CardVO } from '@/types/card'
import { categoryTagStyle } from '@/utils'

/* -------- attribute 属性 -------- */

/**
 * 根据属性值生成 tag 样式
 *
 * @param attribute 属性值：1-Shout 2-Beat 3-Melody
 * @returns 内联样式对象，无效值返回 undefined
 */
export function getAttributeTagStyle(attribute?: number | null) {
  if (attribute == null) return undefined
  const key = attribute as CardAttributeValue
  const paletteKey = CARD_ATTRIBUTE_TAG_TYPE[key]
  if (!paletteKey) return undefined
  return categoryTagStyle(paletteKey)
}

/* -------- 按 maxRarity 决定封面 -------- */
export function getCoverImage(row: CardVO): string | undefined {
  const images = row.images ?? []
  const byType = (t: number) => images.find((i) => i.imageType === t)?.url

  if (row.maxRarity === CARD_MAX_RARITY.UR) {
    // UR：优先竖卡 → 横卡
    return byType(CARD_IMAGE_TYPE.UR_VERTICAL) ?? byType(CARD_IMAGE_TYPE.UR_HORIZONTAL)
  }
  // 非 UR：优先普通 SSR → SSR隐藏款 → SR
  return (
    byType(CARD_IMAGE_TYPE.SSR) ?? byType(CARD_IMAGE_TYPE.SSR_HIDDEN) ?? byType(CARD_IMAGE_TYPE.SR)
  )
}

export function getImageUrls(row: CardVO): string[] {
  return (row.images ?? []).map((i) => i.url!).filter((u) => !!u)
}

/* -------- 附属剧情 -------- */

export function attachedStoryTypeTag(
  type: number,
): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (type) {
    case CARD_ATTACHED_STORY_TYPE.RC:
      return 'success'
    case CARD_ATTACHED_STORY_TYPE.RTV:
      return 'warning'
    case CARD_ATTACHED_STORY_TYPE.RABBITTER:
      return 'info'
    default:
      return 'info'
  }
}
