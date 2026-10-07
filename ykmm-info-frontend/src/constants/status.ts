/* -------- 审核状态 -------- */
export const STATUS = {
  PUBLISHED: 1,
  PENDING: 2,
  REJECTED: 3,
} as const

export type StatusValue = (typeof STATUS)[keyof typeof STATUS]

export const STATUS_LABEL: Record<StatusValue, string> = {
  [STATUS.PUBLISHED]: '已发布',
  [STATUS.PENDING]: '待审核',
  [STATUS.REJECTED]: '已拒绝',
}

export const STATUS_OPTIONS = [
  { value: STATUS.PUBLISHED, label: STATUS_LABEL[STATUS.PUBLISHED] },
  { value: STATUS.PENDING, label: STATUS_LABEL[STATUS.PENDING] },
  { value: STATUS.REJECTED, label: STATUS_LABEL[STATUS.REJECTED] },
] as const

/** el-tag 的 type 映射 */
export const STATUS_TAG_TYPE: Record<StatusValue, 'success' | 'warning' | 'danger'> = {
  [STATUS.PUBLISHED]: 'success',
  [STATUS.PENDING]: 'warning',
  [STATUS.REJECTED]: 'danger',
}

export function statusLabel(v?: number | null): string {
  if (v == null) return '未知'
  return STATUS_LABEL[v as StatusValue] ?? '未知'
}
