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
  return STATUS_TAG_TYPE[v as StatusValue] ?? '未知'
}

/* -------- 审核结果（仅通过 / 拒绝） -------- */
export const REVIEW_STATUS = {
  PUBLISHED: STATUS.PUBLISHED,
  REJECTED: STATUS.REJECTED,
} as const

/** 审核结果只能是通过(1)或拒绝(3)，不含待审核(2) */
export type ReviewStatusValue = (typeof REVIEW_STATUS)[keyof typeof REVIEW_STATUS]

/** 审核弹窗的 radio 选项 */
export const REVIEW_STATUS_OPTIONS = [
  { value: REVIEW_STATUS.PUBLISHED, label: STATUS_LABEL[STATUS.PUBLISHED] },
  { value: REVIEW_STATUS.REJECTED, label: STATUS_LABEL[STATUS.REJECTED] },
] as const

/** 类型守卫：判断某个值是否是合法的审核结果 */
export function isReviewStatus(v: unknown): v is ReviewStatusValue {
  return v === REVIEW_STATUS.PUBLISHED || v === REVIEW_STATUS.REJECTED
}
