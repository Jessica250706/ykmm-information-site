/**
 * 卡面最高等级
 */
export const CARD_MAX_RARITY = {
  SSR: 1,
  UR: 2,
} as const

export type CardMaxRarityValue = (typeof CARD_MAX_RARITY)[keyof typeof CARD_MAX_RARITY]

export const CARD_MAX_RARITY_LABEL: Record<CardMaxRarityValue, string> = {
  [CARD_MAX_RARITY.SSR]: 'SSR',
  [CARD_MAX_RARITY.UR]: 'UR',
}

export const CARD_MAX_RARITY_OPTIONS = [
  { value: CARD_MAX_RARITY.SSR, label: CARD_MAX_RARITY_LABEL[CARD_MAX_RARITY.SSR] },
  { value: CARD_MAX_RARITY.UR, label: CARD_MAX_RARITY_LABEL[CARD_MAX_RARITY.UR] },
] as const

/**
 * 卡面属性
 */
export const CARD_ATTRIBUTE = {
  SHOUT: 1,
  BEAT: 2,
  MELODY: 3,
} as const

export type CardAttributeValue = (typeof CARD_ATTRIBUTE)[keyof typeof CARD_ATTRIBUTE]

export const CARD_ATTRIBUTE_LABEL: Record<CardAttributeValue, string> = {
  [CARD_ATTRIBUTE.SHOUT]: 'Shout',
  [CARD_ATTRIBUTE.BEAT]: 'Beat',
  [CARD_ATTRIBUTE.MELODY]: 'Melody',
}

export const CARD_ATTRIBUTE_OPTIONS = [
  { value: CARD_ATTRIBUTE.SHOUT, label: CARD_ATTRIBUTE_LABEL[CARD_ATTRIBUTE.SHOUT] },
  { value: CARD_ATTRIBUTE.BEAT, label: CARD_ATTRIBUTE_LABEL[CARD_ATTRIBUTE.BEAT] },
  { value: CARD_ATTRIBUTE.MELODY, label: CARD_ATTRIBUTE_LABEL[CARD_ATTRIBUTE.MELODY] },
] as const

/** 属性对应颜色（视觉区分） */
export const CARD_ATTRIBUTE_TAG_TYPE: Record<
  CardAttributeValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [CARD_ATTRIBUTE.SHOUT]: 'danger',
  [CARD_ATTRIBUTE.BEAT]: 'success',
  [CARD_ATTRIBUTE.MELODY]: 'primary',
}

/**
 * 卡面图片类型
 * 1-R 2-SR 3-SSR 4-SSR隐藏款 5-UR竖卡 6-UR横卡
 */
export const CARD_IMAGE_TYPE = {
  R: 1,
  SR: 2,
  SSR: 3,
  SSR_HIDDEN: 4,
  UR_VERTICAL: 5,
  UR_HORIZONTAL: 6,
} as const

export type CardImageTypeValue = (typeof CARD_IMAGE_TYPE)[keyof typeof CARD_IMAGE_TYPE]

export const CARD_IMAGE_TYPE_LABEL: Record<CardImageTypeValue, string> = {
  [CARD_IMAGE_TYPE.R]: 'R',
  [CARD_IMAGE_TYPE.SR]: 'SR',
  [CARD_IMAGE_TYPE.SSR]: 'SSR',
  [CARD_IMAGE_TYPE.SSR_HIDDEN]: 'SSR隐藏款',
  [CARD_IMAGE_TYPE.UR_VERTICAL]: 'UR竖卡',
  [CARD_IMAGE_TYPE.UR_HORIZONTAL]: 'UR横卡',
}

export const CARD_IMAGE_TYPE_OPTIONS = [
  { value: CARD_IMAGE_TYPE.R, label: CARD_IMAGE_TYPE_LABEL[CARD_IMAGE_TYPE.R] },
  { value: CARD_IMAGE_TYPE.SR, label: CARD_IMAGE_TYPE_LABEL[CARD_IMAGE_TYPE.SR] },
  { value: CARD_IMAGE_TYPE.SSR, label: CARD_IMAGE_TYPE_LABEL[CARD_IMAGE_TYPE.SSR] },
  {
    value: CARD_IMAGE_TYPE.SSR_HIDDEN,
    label: CARD_IMAGE_TYPE_LABEL[CARD_IMAGE_TYPE.SSR_HIDDEN],
  },
  {
    value: CARD_IMAGE_TYPE.UR_VERTICAL,
    label: CARD_IMAGE_TYPE_LABEL[CARD_IMAGE_TYPE.UR_VERTICAL],
  },
  {
    value: CARD_IMAGE_TYPE.UR_HORIZONTAL,
    label: CARD_IMAGE_TYPE_LABEL[CARD_IMAGE_TYPE.UR_HORIZONTAL],
  },
] as const

/**
 * 附属剧情类型
 */
export const CARD_ATTACHED_STORY_TYPE = {
  NONE: 0,
  RC: 1,
  RTV: 2,
  RABBITTER: 3,
} as const

export type CardAttachedStoryTypeValue =
  (typeof CARD_ATTACHED_STORY_TYPE)[keyof typeof CARD_ATTACHED_STORY_TYPE]

export const CARD_ATTACHED_STORY_TYPE_LABEL: Record<CardAttachedStoryTypeValue, string> = {
  [CARD_ATTACHED_STORY_TYPE.NONE]: '无',
  [CARD_ATTACHED_STORY_TYPE.RC]: 'RC',
  [CARD_ATTACHED_STORY_TYPE.RTV]: 'RTV',
  [CARD_ATTACHED_STORY_TYPE.RABBITTER]: 'Rabbiter',
}

export const CARD_ATTACHED_STORY_TYPE_OPTIONS = [
  { value: CARD_ATTACHED_STORY_TYPE.NONE, label: CARD_ATTACHED_STORY_TYPE_LABEL[0] },
  { value: CARD_ATTACHED_STORY_TYPE.RC, label: CARD_ATTACHED_STORY_TYPE_LABEL[1] },
  { value: CARD_ATTACHED_STORY_TYPE.RTV, label: CARD_ATTACHED_STORY_TYPE_LABEL[2] },
  { value: CARD_ATTACHED_STORY_TYPE.RABBITTER, label: CARD_ATTACHED_STORY_TYPE_LABEL[3] },
] as const

/**
 * 服装类型
 */
export const CARD_COSTUME_TYPE = {
  CHIBI: 1,
  MODEL_3D: 2,
} as const

export type CardCostumeTypeValue = (typeof CARD_COSTUME_TYPE)[keyof typeof CARD_COSTUME_TYPE]

export const CARD_COSTUME_TYPE_LABEL: Record<CardCostumeTypeValue, string> = {
  [CARD_COSTUME_TYPE.CHIBI]: '偶像小人',
  [CARD_COSTUME_TYPE.MODEL_3D]: '3D造型',
}

export const CARD_COSTUME_TYPE_OPTIONS = [
  { value: CARD_COSTUME_TYPE.CHIBI, label: CARD_COSTUME_TYPE_LABEL[CARD_COSTUME_TYPE.CHIBI] },
  { value: CARD_COSTUME_TYPE.MODEL_3D, label: CARD_COSTUME_TYPE_LABEL[CARD_COSTUME_TYPE.MODEL_3D] },
] as const

/**
 * 卡面状态
 */
export const CARD_STATUS = {
  PUBLISHED: 1,
  PENDING: 2,
  REJECTED: 3,
} as const

export type CardStatusValue = (typeof CARD_STATUS)[keyof typeof CARD_STATUS]

export const CARD_STATUS_LABEL: Record<CardStatusValue, string> = {
  [CARD_STATUS.PUBLISHED]: '已发布',
  [CARD_STATUS.PENDING]: '待审核',
  [CARD_STATUS.REJECTED]: '已拒绝',
}

export const CARD_STATUS_OPTIONS = [
  { value: CARD_STATUS.PUBLISHED, label: CARD_STATUS_LABEL[CARD_STATUS.PUBLISHED] },
  { value: CARD_STATUS.PENDING, label: CARD_STATUS_LABEL[CARD_STATUS.PENDING] },
  { value: CARD_STATUS.REJECTED, label: CARD_STATUS_LABEL[CARD_STATUS.REJECTED] },
] as const

/** 安全取文案 */
export function cardMaxRarityLabel(v?: number | null): string {
  return v != null && (v === 1 || v === 2) ? CARD_MAX_RARITY_LABEL[v] : '未知'
}

export function cardAttributeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return CARD_ATTRIBUTE_LABEL[v as CardAttributeValue] ?? '未知'
}

export function cardImageTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return CARD_IMAGE_TYPE_LABEL[v as CardImageTypeValue] ?? '未知'
}

export function cardAttachedStoryTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return CARD_ATTACHED_STORY_TYPE_LABEL[v as CardAttachedStoryTypeValue] ?? '未知'
}

export function cardCostumeTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return CARD_COSTUME_TYPE_LABEL[v as CardCostumeTypeValue] ?? '未知'
}

export function cardStatusLabel(v?: number | null): string {
  if (v == null) return '未知'
  return CARD_STATUS_LABEL[v as CardStatusValue] ?? '未知'
}
