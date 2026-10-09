/** 编辑器模式 */
export const EDITOR_MODE = {
  EDIT: 'edit',
  CREATE: 'create',
} as const

export type EditorMode = (typeof EDITOR_MODE)[keyof typeof EDITOR_MODE]

export const EDITOR_MODE_LABEL: Record<EditorMode, string> = {
  [EDITOR_MODE.EDIT]: '编辑',
  [EDITOR_MODE.CREATE]: '新增',
}

/** 是否编辑模式 */
export function isEditMode(mode: EditorMode): boolean {
  return mode === EDITOR_MODE.EDIT
}

/** 是否新增模式 */
export function isCreateMode(mode: EditorMode): boolean {
  return mode === EDITOR_MODE.CREATE
}

/** 编辑目标 */
export const EDIT_TARGET = {
  NORMAL: 'normal',
  RC_OPTION: 'rcOption',
} as const

export type EditTarget = (typeof EDIT_TARGET)[keyof typeof EDIT_TARGET]

export const EDIT_TARGET_LABEL: Record<EditTarget, string> = {
  [EDIT_TARGET.NORMAL]: '普通行',
  [EDIT_TARGET.RC_OPTION]: 'RC 选项',
}

/** 是否普通行编辑 */
export function isNormalTarget(target: EditTarget): boolean {
  return target === EDIT_TARGET.NORMAL
}

/** 是否 RC 选项编辑 */
export function isRcOptionTarget(target: EditTarget): boolean {
  return target === EDIT_TARGET.RC_OPTION
}
