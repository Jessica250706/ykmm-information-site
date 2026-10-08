import {
  CARD_ATTRIBUTE_TAG_TYPE,
  CARD_IMAGE_TYPE,
  CARD_MAX_RARITY,
  CARD_MAX_RARITY_TAG_TYPE,
  type CardAttributeValue,
  DEFAULT_TAG_TYPE,
  SOURCE_TYPE_LABEL,
  SOURCE_TYPE_SMALL_LABEL,
  type SourceTypeSmallLabelValue,
  type SourceTypeValue,
  type TagType,
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

/**
 * 根据卡面最高等级获取 Tag type
 *
 * @param maxRarity 卡面最高等级
 * @returns Element Plus Tag type：'danger' | 'warning' | 'info' | ...
 */
export function getCardRarityTagType(maxRarity?: number | null): TagType {
  if (maxRarity == null) return DEFAULT_TAG_TYPE
  return CARD_MAX_RARITY_TAG_TYPE[maxRarity] ?? DEFAULT_TAG_TYPE
}

/**
 * small label → 完整 label 反查表
 * 由 SOURCE_TYPE_SMALL_LABEL 和 SOURCE_TYPE_LABEL 自动生成，避免两处维护
 */
export const SOURCE_TYPE_LABEL_BY_SMALL: Record<SourceTypeSmallLabelValue, string> = (
  Object.keys(SOURCE_TYPE_SMALL_LABEL) as unknown as SourceTypeValue[]
).reduce(
  (acc, key) => {
    acc[SOURCE_TYPE_SMALL_LABEL[key]] = SOURCE_TYPE_LABEL[key]
    return acc
  },
  {} as Record<SourceTypeSmallLabelValue, string>,
)

/**
 * 根据 small label 获取完整 label
 *
 * @param mode small label，如 'rc' / 'rtv'
 * @returns 完整 label，如 'RC' / 'RTV'；未知返回 ''
 */
export function getSourceTypeLabel(mode: SourceTypeSmallLabelValue): string {
  return SOURCE_TYPE_LABEL_BY_SMALL[mode] ?? ''
}
