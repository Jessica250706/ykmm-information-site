export const MENU_TYPE = {
  ADMIN: 1,
  USER: 2,
} as const

export type MenuTypeValue = (typeof MENU_TYPE)[keyof typeof MENU_TYPE]

export const MENU_TYPE_LABEL: Record<MenuTypeValue, string> = {
  [MENU_TYPE.ADMIN]: '管理端',
  [MENU_TYPE.USER]: '用户端',
}

/** 下拉选项，给 el-select 直接用 */
export const MENU_TYPE_OPTIONS = [
  { value: MENU_TYPE.ADMIN, label: MENU_TYPE_LABEL[MENU_TYPE.ADMIN] },
  { value: MENU_TYPE.USER, label: MENU_TYPE_LABEL[MENU_TYPE.USER] },
] as const

/** 类型守卫：判断是否合法的 menuType */
export function isMenuType(v: unknown): v is MenuTypeValue {
  return v === MENU_TYPE.ADMIN || v === MENU_TYPE.USER
}

/** 安全取文案 */
export function menuTypeLabel(v?: number): string {
  return v != null && isMenuType(v) ? MENU_TYPE_LABEL[v] : '未知'
}

/* -------- 是否显示 -------- */
export const MENU_VISIBLE = {
  HIDDEN: 0,
  VISIBLE: 1,
} as const

export type MenuVisibleValue = (typeof MENU_VISIBLE)[keyof typeof MENU_VISIBLE]

export const MENU_VISIBLE_LABEL: Record<MenuVisibleValue, string> = {
  [MENU_VISIBLE.HIDDEN]: '隐藏',
  [MENU_VISIBLE.VISIBLE]: '显示',
}
