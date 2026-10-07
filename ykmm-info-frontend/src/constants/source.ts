/**
 * 来源类型
 * 0-无 1-RC 2-RTV 3-Rabbitter 4-剧情
 */
export const SOURCE_TYPE = {
  NONE: 0,
  RC: 1,
  RTV: 2,
  RABBITTER: 3,
  STORY: 4,
} as const

export type SourceTypeValue = (typeof SOURCE_TYPE)[keyof typeof SOURCE_TYPE]

export const SOURCE_TYPE_LABEL: Record<SourceTypeValue, string> = {
  [SOURCE_TYPE.NONE]: '无',
  [SOURCE_TYPE.RC]: 'RC',
  [SOURCE_TYPE.RTV]: 'RTV',
  [SOURCE_TYPE.RABBITTER]: 'Rabbitter',
  [SOURCE_TYPE.STORY]: '剧情',
}

/** 下拉选项，给 el-select 直接用 */
export const SOURCE_TYPE_OPTIONS = [
  { value: SOURCE_TYPE.NONE, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.NONE] },
  { value: SOURCE_TYPE.RC, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RC] },
  { value: SOURCE_TYPE.RTV, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV] },
  { value: SOURCE_TYPE.RABBITTER, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RABBITTER] },
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
  [SOURCE_TYPE.RABBITTER]: 'primary',
  [SOURCE_TYPE.STORY]: 'danger',
}

/** 类型守卫 */
export function isSourceType(v: unknown): v is SourceTypeValue {
  return (
    v === SOURCE_TYPE.STORY ||
    v === SOURCE_TYPE.RTV ||
    v === SOURCE_TYPE.RC ||
    v === SOURCE_TYPE.NONE ||
    v === SOURCE_TYPE.RABBITTER
  )
}

export function cardAttachedStoryTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return SOURCE_TYPE_LABEL[v as SourceTypeValue] ?? '未知'
}

/** 安全取文案（插槽 row 是 any 时用它兜底） */
export function sourceTypeLabel(v?: number): string {
  return v != null && isSourceType(v) ? SOURCE_TYPE_LABEL[v] : '未知'
}
