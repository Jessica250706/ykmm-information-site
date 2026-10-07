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

/**
 * 判断值是否为合法的状态枚举
 *
 * @param v 待判断值
 */
export function isStatus(v: unknown): v is StatusValue {
  return v === STATUS.PUBLISHED || v === STATUS.PENDING || v === STATUS.REJECTED
}

/**
 * 根据状态值生成 el-tag 的 type
 *
 * @param status 状态值：1已发布 2待审核 3已拒绝
 * @returns el-tag 的 type，无效值返回 'info'
 */
export function getStatusTagType(status?: number | null) {
  if (status == null) return 'info'
  const key = status as StatusValue
  return STATUS_TAG_TYPE[key] ?? 'info'
}
