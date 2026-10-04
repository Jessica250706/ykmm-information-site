/**
 * 对话是否内心独白
 * 0-说出来的话 1-内心独白
 */
export const MONOLOGUE = {
  SPOKEN: 0,
  INNER: 1,
} as const

export type MonologueValue = (typeof MONOLOGUE)[keyof typeof MONOLOGUE]

export const MONOLOGUE_LABEL: Record<MonologueValue, string> = {
  [MONOLOGUE.SPOKEN]: '说出来的话',
  [MONOLOGUE.INNER]: '内心独白',
}

/** 下拉选项，给 el-select 直接用 */
export const MONOLOGUE_OPTIONS = [
  { value: MONOLOGUE.SPOKEN, label: MONOLOGUE_LABEL[MONOLOGUE.SPOKEN] },
  { value: MONOLOGUE.INNER, label: MONOLOGUE_LABEL[MONOLOGUE.INNER] },
] as const

/** el-tag 的 type 映射（可选） */
export const MONOLOGUE_TAG_TYPE: Record<
  MonologueValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [MONOLOGUE.SPOKEN]: 'primary',
  [MONOLOGUE.INNER]: 'info',
}

/** 类型守卫 */
export function isMonologue(v: unknown): v is MonologueValue {
  return v === MONOLOGUE.SPOKEN || v === MONOLOGUE.INNER
}

/** 是否内心独白（含 null 兼容） */
export function isInnerMonologue(v?: number | null): boolean {
  return v === MONOLOGUE.INNER
}

/** 安全取文案（row 是 any 时兜底） */
export function monologueLabel(v?: number | null): string {
  return v != null && isMonologue(v) ? MONOLOGUE_LABEL[v] : MONOLOGUE_LABEL[MONOLOGUE.SPOKEN]
}
