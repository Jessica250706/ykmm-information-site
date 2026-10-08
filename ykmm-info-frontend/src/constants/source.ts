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
}
export type SourceTypeLabelValue = (typeof SOURCE_TYPE_LABEL)[keyof typeof SOURCE_TYPE_LABEL]

/** 下拉选项，给 el-select 直接用 */
export const SOURCE_TYPE_OPTIONS = [
  { value: SOURCE_TYPE.NONE, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.NONE] },
  { value: SOURCE_TYPE.RC, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RC] },
  { value: SOURCE_TYPE.RTV, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV] },
  { value: SOURCE_TYPE.RABITTER, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RABITTER] },
  { value: SOURCE_TYPE.STORY, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.STORY] },
] as const

/** el-tag 的 type 映射（可选，视视觉需要调整） */
export const SOURCE_TYPE_TAG_TYPE: Record<
  SourceTypeValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
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

export function cardAttachedStoryTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return SOURCE_TYPE_LABEL[v as SourceTypeValue] ?? '未知'
}

export function cardAttachedStoryTypeTagLabel(
  v?: number | null,
): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  if (v == null) return 'info'
  return SOURCE_TYPE_TAG_TYPE[v as SourceTypeValue] ?? 'info'
}

/** 安全取文案（插槽 row 是 any 时用它兜底） */
export function sourceTypeLabel(v?: number): string {
  return v != null && isSourceType(v) ? SOURCE_TYPE_LABEL[v] : '未知'
}
