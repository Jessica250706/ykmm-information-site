/**
 * 用户角色
 * - ADMIN：管理员
 * - USER：普通用户
 */
export const ROLE = {
  ADMIN: 1,
  USER: 2,
} as const

/** 角色值的联合类型：1 | 2 */
export type RoleValue = (typeof ROLE)[keyof typeof ROLE]

/** 角色中文名映射，用于表格展示 / 下拉选项 */
export const ROLE_LABEL: Record<RoleValue, string> = {
  [ROLE.ADMIN]: '管理员',
  [ROLE.USER]: '普通用户',
}

/** 角色下拉选项，直接给 el-select 用 */
export const ROLE_OPTIONS = [
  { label: ROLE_LABEL[ROLE.ADMIN], value: ROLE.ADMIN },
  { label: ROLE_LABEL[ROLE.USER], value: ROLE.USER },
] as const
