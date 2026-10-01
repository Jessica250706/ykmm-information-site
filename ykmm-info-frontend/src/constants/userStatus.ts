export const USER_STATUS = {
  DISABLED: 0,
  ENABLED: 1,
} as const

export type UserStatusValue = (typeof USER_STATUS)[keyof typeof USER_STATUS]

export const USER_STATUS_LABEL: Record<UserStatusValue, string> = {
  [USER_STATUS.DISABLED]: '禁用',
  [USER_STATUS.ENABLED]: '启用',
}

export const USER_STATUS_ACTION_LABEL: Record<UserStatusValue, string> = {
  [USER_STATUS.ENABLED]: '禁用', // 当前启用 → 按钮显示"禁用"
  [USER_STATUS.DISABLED]: '启用', // 当前禁用 → 按钮显示"启用"
}
