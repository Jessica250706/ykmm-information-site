import { type TagType, UserRouteName } from './index'

/**
 * 来源类型
 * 0-无 1-RC 2-RTV 3-Rabitter 4-剧情
 */
export const SOURCE_TYPE = {
  NONE: 0,
  RC: 1,
  RTV: 2,
  RABITTER: 3,
  STORY: 4,
} as const

export type SourceTypeValue = (typeof SOURCE_TYPE)[keyof typeof SOURCE_TYPE]

export const SOURCE_TYPE_LABEL: Record<SourceTypeValue, string> = {
  [SOURCE_TYPE.NONE]: '无',
  [SOURCE_TYPE.RC]: 'RC',
  [SOURCE_TYPE.RTV]: 'RTV',
  [SOURCE_TYPE.RABITTER]: 'Rabitter',
  [SOURCE_TYPE.STORY]: '剧情',
} as const

export type SourceTypeLabelValue = (typeof SOURCE_TYPE_LABEL)[keyof typeof SOURCE_TYPE_LABEL]

export const SOURCE_TYPE_SMALL_LABEL: Record<SourceTypeValue, string> = {
  [SOURCE_TYPE.NONE]: 'none',
  [SOURCE_TYPE.RC]: 'rc',
  [SOURCE_TYPE.RTV]: 'rtv',
  [SOURCE_TYPE.RABITTER]: 'rabitter',
  [SOURCE_TYPE.STORY]: 'story',
}

export type SourceTypeSmallLabelValue =
  (typeof SOURCE_TYPE_SMALL_LABEL)[keyof typeof SOURCE_TYPE_SMALL_LABEL]

/** 下拉选项，给 el-select 直接用 */
export const SOURCE_TYPE_OPTIONS = [
  { value: SOURCE_TYPE.NONE, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.NONE] },
  { value: SOURCE_TYPE.RC, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RC] },
  { value: SOURCE_TYPE.RTV, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV] },
  { value: SOURCE_TYPE.RABITTER, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RABITTER] },
  { value: SOURCE_TYPE.STORY, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.STORY] },
] as const

/** el-tag 的 type 映射 */
export const SOURCE_TYPE_TAG_TYPE: Record<SourceTypeValue, TagType> = {
  [SOURCE_TYPE.NONE]: 'info',
  [SOURCE_TYPE.RC]: 'warning',
  [SOURCE_TYPE.RTV]: 'success',
  [SOURCE_TYPE.RABITTER]: 'primary',
  [SOURCE_TYPE.STORY]: 'info',
}

/** 类型守卫 */
export function isSourceType(v: unknown): v is SourceTypeValue {
  return (
    v === SOURCE_TYPE.STORY ||
    v === SOURCE_TYPE.RTV ||
    v === SOURCE_TYPE.RC ||
    v === SOURCE_TYPE.NONE ||
    v === SOURCE_TYPE.RABITTER
  )
}

/**
 * 来源类型筛选下拉（排除 NONE，因为版本不会有"无来源"）
 */
export const SOURCE_TYPE_FILTER_OPTIONS = [
  { value: SOURCE_TYPE.RC, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RC] },
  { value: SOURCE_TYPE.RTV, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV] },
  { value: SOURCE_TYPE.RABITTER, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RABITTER] },
  { value: SOURCE_TYPE.STORY, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.STORY] },
] as const

/* ============================================================
 * 卡面附属剧情（card.attached_story_type）
 *   0-无 1-RC 2-RTV 3-Rabitter
 *   ★ 不含 STORY：剧情是独立来源，不是「卡面附属」剧情
 * ============================================================ */

/**
 * 卡面附属剧情类型。
 * <p>是 {@link SourceTypeValue} 的子集，显式排除 {@link SOURCE_TYPE.STORY}。
 * <p>用 Exclude 而非硬编码，意味着未来 SourceTypeValue 新增值时，
 * 这里会因"未显式声明"而让调用方得到类型提示，强制审查是否应纳入。
 */
export type CardAttachedStoryTypeValue = Exclude<SourceTypeValue, typeof SOURCE_TYPE.STORY>

/**
 * 卡面附属剧情标签。
 * <p>与 {@link SOURCE_TYPE_LABEL} 的子集部分完全一致，单独抽出来是为了让
 * 索引时的 key 被约束在 0/1/2/3 范围内——传入 STORY 会被 TS 直接报错。
 */
export const CARD_ATTACHED_STORY_TYPE_LABEL: Record<CardAttachedStoryTypeValue, string> = {
  [SOURCE_TYPE.NONE]: SOURCE_TYPE_LABEL[SOURCE_TYPE.NONE],
  [SOURCE_TYPE.RC]: SOURCE_TYPE_LABEL[SOURCE_TYPE.RC],
  [SOURCE_TYPE.RTV]: SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV],
  [SOURCE_TYPE.RABITTER]: SOURCE_TYPE_LABEL[SOURCE_TYPE.RABITTER],
}

/**
 * 卡面附属剧情筛选/编辑下拉。
 * <p>保留 NONE（"无"是有效选项，用于筛"没有附属剧情的卡面"）；
 * 排除 STORY（剧情不是卡面附属剧情）。
 */
export const CARD_ATTACHED_STORY_TYPE_OPTIONS = [
  { value: SOURCE_TYPE.NONE, label: CARD_ATTACHED_STORY_TYPE_LABEL[SOURCE_TYPE.NONE] },
  { value: SOURCE_TYPE.RC, label: CARD_ATTACHED_STORY_TYPE_LABEL[SOURCE_TYPE.RC] },
  { value: SOURCE_TYPE.RTV, label: CARD_ATTACHED_STORY_TYPE_LABEL[SOURCE_TYPE.RTV] },
  { value: SOURCE_TYPE.RABITTER, label: CARD_ATTACHED_STORY_TYPE_LABEL[SOURCE_TYPE.RABITTER] },
] as const

export function sourceTypeBrowseRouteName(v?: number | null): string | undefined {
  if (v == null) return undefined
  switch (v) {
    case SOURCE_TYPE.RC:
      return UserRouteName.CARD_RC_BROWSE
    case SOURCE_TYPE.RTV:
      return UserRouteName.CARD_RTV_BROWSE
    case SOURCE_TYPE.RABITTER:
      return UserRouteName.CARD_RABITTER_BROWSE
    default:
      return undefined
  }
}

export function cardAttachedStoryTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return SOURCE_TYPE_LABEL[v as SourceTypeValue] ?? '未知'
}

export function cardAttachedStoryTypeTagLabel(v?: number | null): TagType {
  if (v == null) return 'info'
  return SOURCE_TYPE_TAG_TYPE[v as SourceTypeValue] ?? 'info'
}

/** 安全取文案（插槽 row 是 any 时用它兜底） */
export function sourceTypeLabel(v?: number): string {
  return v != null && isSourceType(v) ? SOURCE_TYPE_LABEL[v] : '未知'
}
