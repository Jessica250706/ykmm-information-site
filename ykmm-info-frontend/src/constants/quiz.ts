/* -------- 题型 -------- */
export const QUESTION_TYPE = {
  SINGLE: 1,
  MULTI: 2,
} as const

export type QuestionTypeValue = (typeof QUESTION_TYPE)[keyof typeof QUESTION_TYPE]

export const QUESTION_TYPE_LABEL: Record<number, string> = {
  [QUESTION_TYPE.SINGLE]: '单选',
  [QUESTION_TYPE.MULTI]: '多选',
}

export const QUESTION_TYPE_OPTIONS = [
  { value: QUESTION_TYPE.SINGLE, label: '单选' },
  { value: QUESTION_TYPE.MULTI, label: '多选' },
] as const

/* -------- 题目类型（固定/随机） -------- */
export const QUESTION_FIXED = {
  NO: 0,
  YES: 1,
} as const

export type QuestionFixedValue = (typeof QUESTION_FIXED)[keyof typeof QUESTION_FIXED]

export const QUESTION_FIXED_LABEL: Record<number, string> = {
  [QUESTION_FIXED.NO]: '随机题',
  [QUESTION_FIXED.YES]: '固定题',
}

export const QUESTION_FIXED_OPTIONS = [
  { value: QUESTION_FIXED.NO, label: '随机题' },
  { value: QUESTION_FIXED.YES, label: '固定题' },
] as const

/* -------- 题目状态 -------- */
export const QUESTION_STATUS = {
  DISABLED: 0,
  ENABLED: 1,
} as const

export type QuestionStatusValue = (typeof QUESTION_STATUS)[keyof typeof QUESTION_STATUS]

export const QUESTION_STATUS_LABEL: Record<number, string> = {
  [QUESTION_STATUS.DISABLED]: '禁用',
  [QUESTION_STATUS.ENABLED]: '启用',
}

export const QUESTION_STATUS_OPTIONS = [
  { value: QUESTION_STATUS.DISABLED, label: '禁用' },
  { value: QUESTION_STATUS.ENABLED, label: '启用' },
] as const

export const QUESTION_STATUS_TAG_TYPE: Record<number, 'info' | 'success'> = {
  [QUESTION_STATUS.DISABLED]: 'info',
  [QUESTION_STATUS.ENABLED]: 'success',
}

/* -------- 答题尝试状态 -------- */
export const QUIZ_ATTEMPT_STATUS = {
  IN_PROGRESS: 0,
  PASSED: 1,
  FAILED: 2,
} as const

export const QUIZ_ATTEMPT_STATUS_LABEL: Record<number, string> = {
  [QUIZ_ATTEMPT_STATUS.IN_PROGRESS]: '进行中',
  [QUIZ_ATTEMPT_STATUS.PASSED]: '已通过',
  [QUIZ_ATTEMPT_STATUS.FAILED]: '未通过',
}

export const QUIZ_ATTEMPT_STATUS_TAG_TYPE: Record<number, 'info' | 'success' | 'danger'> = {
  [QUIZ_ATTEMPT_STATUS.IN_PROGRESS]: 'info',
  [QUIZ_ATTEMPT_STATUS.PASSED]: 'success',
  [QUIZ_ATTEMPT_STATUS.FAILED]: 'danger',
}

/* -------- 工具函数 -------- */
export function questionTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return QUESTION_TYPE_LABEL[v] ?? '未知'
}

export function questionFixedLabel(v?: number | null): string {
  if (v == null) return '未知'
  return QUESTION_FIXED_LABEL[v] ?? '未知'
}

export function questionStatusLabel(v?: number | null): string {
  if (v == null) return '未知'
  return QUESTION_STATUS_LABEL[v] ?? '未知'
}

export function quizAttemptStatusLabel(v?: number | null): string {
  if (v == null) return '未知'
  return QUIZ_ATTEMPT_STATUS_LABEL[v] ?? '未知'
}
