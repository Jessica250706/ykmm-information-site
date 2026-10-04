/**
 * 来源类型
 * 1-剧情 2-RTV 3-RC
 */
export const SOURCE_TYPE = {
  STORY: 1,
  RTV: 2,
  RC: 3,
} as const

export type SourceTypeValue = (typeof SOURCE_TYPE)[keyof typeof SOURCE_TYPE]

export const SOURCE_TYPE_LABEL: Record<SourceTypeValue, string> = {
  [SOURCE_TYPE.STORY]: '剧情',
  [SOURCE_TYPE.RTV]: 'RTV',
  [SOURCE_TYPE.RC]: 'RC',
}

/** 下拉选项，给 el-select 直接用 */
export const SOURCE_TYPE_OPTIONS = [
  { value: SOURCE_TYPE.STORY, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.STORY] },
  { value: SOURCE_TYPE.RTV, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV] },
  { value: SOURCE_TYPE.RC, label: SOURCE_TYPE_LABEL[SOURCE_TYPE.RC] },
] as const

/** el-tag 的 type 映射（可选，视视觉需要调整） */
export const SOURCE_TYPE_TAG_TYPE: Record<
  SourceTypeValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [SOURCE_TYPE.STORY]: 'primary',
  [SOURCE_TYPE.RTV]: 'success',
  [SOURCE_TYPE.RC]: 'warning',
}

/** 类型守卫 */
export function isSourceType(v: unknown): v is SourceTypeValue {
  return v === SOURCE_TYPE.STORY || v === SOURCE_TYPE.RTV || v === SOURCE_TYPE.RC
}

/** 安全取文案（插槽 row 是 any 时用它兜底） */
export function sourceTypeLabel(v?: number): string {
  return v != null && isSourceType(v) ? SOURCE_TYPE_LABEL[v] : '未知'
}
