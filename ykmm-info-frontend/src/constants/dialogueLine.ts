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

/**
 * 对话左右位置（RC 聊天用）
 * 1-左侧 2-右侧
 */
export const DIALOGUE_SIDE = {
  LEFT: 1,
  RIGHT: 2,
} as const

export type DialogueSideValue = (typeof DIALOGUE_SIDE)[keyof typeof DIALOGUE_SIDE]

export const DIALOGUE_SIDE_LABEL: Record<DialogueSideValue, string> = {
  [DIALOGUE_SIDE.LEFT]: '左侧',
  [DIALOGUE_SIDE.RIGHT]: '右侧',
}

export const DIALOGUE_SIDE_OPTIONS = [
  { value: DIALOGUE_SIDE.LEFT, label: DIALOGUE_SIDE_LABEL[DIALOGUE_SIDE.LEFT] },
  { value: DIALOGUE_SIDE.RIGHT, label: DIALOGUE_SIDE_LABEL[DIALOGUE_SIDE.RIGHT] },
] as const

/** 是否右侧（RC 聊天里"我"这一侧） */
export function isRightSide(side?: number | null): boolean {
  return side === DIALOGUE_SIDE.RIGHT
}

/** 是否左侧 */
export function isLeftSide(side?: number | null): boolean {
  return side === DIALOGUE_SIDE.LEFT
}

/** 安全取文案 */
export function dialogueSideLabel(side?: number | null): string {
  return isRightSide(side)
    ? DIALOGUE_SIDE_LABEL[DIALOGUE_SIDE.RIGHT]
    : DIALOGUE_SIDE_LABEL[DIALOGUE_SIDE.LEFT]
}
