/**
 * 消息类型
 */
export const MESSAGE_TYPE = {
  REVIEW_PASSED: 1,
  REVIEW_REJECTED: 2,
  SYSTEM: 3,
} as const

export type MessageTypeValue = (typeof MESSAGE_TYPE)[keyof typeof MESSAGE_TYPE]

export const MESSAGE_TYPE_LABEL: Record<number, string> = {
  [MESSAGE_TYPE.REVIEW_PASSED]: '审核通过',
  [MESSAGE_TYPE.REVIEW_REJECTED]: '审核拒绝',
  [MESSAGE_TYPE.SYSTEM]: '系统通知',
}

export const MESSAGE_TYPE_OPTIONS = [
  { value: MESSAGE_TYPE.REVIEW_PASSED, label: MESSAGE_TYPE_LABEL[MESSAGE_TYPE.REVIEW_PASSED] },
  { value: MESSAGE_TYPE.REVIEW_REJECTED, label: MESSAGE_TYPE_LABEL[MESSAGE_TYPE.REVIEW_REJECTED] },
  { value: MESSAGE_TYPE.SYSTEM, label: MESSAGE_TYPE_LABEL[MESSAGE_TYPE.SYSTEM] },
] as const

/** 消息类型对应的 el-tag type */
export const MESSAGE_TYPE_TAG_TYPE: Record<number, 'success' | 'danger' | 'info'> = {
  [MESSAGE_TYPE.REVIEW_PASSED]: 'success',
  [MESSAGE_TYPE.REVIEW_REJECTED]: 'danger',
  [MESSAGE_TYPE.SYSTEM]: 'info',
}

export function messageTypeLabel(v?: number | null): string {
  if (v == null) return '未知'
  return MESSAGE_TYPE_LABEL[v] ?? '未知'
}

/**
 * 关联业务类型
 */
export const MESSAGE_TARGET_TYPE = {
  CARD: 1,
  DIALOGUE_VERSION: 2,
  STORY: 3,
} as const

export type MessageTargetTypeValue = (typeof MESSAGE_TARGET_TYPE)[keyof typeof MESSAGE_TARGET_TYPE]

export const MESSAGE_TARGET_TYPE_LABEL: Record<number, string> = {
  [MESSAGE_TARGET_TYPE.CARD]: '卡面',
  [MESSAGE_TARGET_TYPE.DIALOGUE_VERSION]: '对话版本',
  [MESSAGE_TARGET_TYPE.STORY]: '剧情',
}

export const MESSAGE_TARGET_TYPE_OPTIONS = [
  { value: MESSAGE_TARGET_TYPE.CARD, label: MESSAGE_TARGET_TYPE_LABEL[MESSAGE_TARGET_TYPE.CARD] },
  {
    value: MESSAGE_TARGET_TYPE.DIALOGUE_VERSION,
    label: MESSAGE_TARGET_TYPE_LABEL[MESSAGE_TARGET_TYPE.DIALOGUE_VERSION],
  },
  { value: MESSAGE_TARGET_TYPE.STORY, label: MESSAGE_TARGET_TYPE_LABEL[MESSAGE_TARGET_TYPE.STORY] },
] as const

export function messageTargetTypeLabel(v?: number | null): string {
  if (v == null) return '-'
  return MESSAGE_TARGET_TYPE_LABEL[v] ?? '-'
}

/**
 * 已读状态
 */
export const MESSAGE_READ_STATUS = {
  UNREAD: 0,
  READ: 1,
} as const

export const MESSAGE_READ_OPTIONS = [
  { value: MESSAGE_READ_STATUS.UNREAD, label: '未读' },
  { value: MESSAGE_READ_STATUS.READ, label: '已读' },
] as const
