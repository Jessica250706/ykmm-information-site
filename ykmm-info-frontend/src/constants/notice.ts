/** 公告状态 */
export const NOTICE_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  OFFLINE: 2,
  SCHEDULED: 3,
} as const

export const NOTICE_STATUS_LABEL: Record<number, string> = {
  [NOTICE_STATUS.DRAFT]: '草稿',
  [NOTICE_STATUS.PUBLISHED]: '已发布',
  [NOTICE_STATUS.OFFLINE]: '已下线',
  [NOTICE_STATUS.SCHEDULED]: '待发布',
}

export const NOTICE_STATUS_OPTIONS = [
  { value: NOTICE_STATUS.DRAFT, label: '草稿' },
  { value: NOTICE_STATUS.PUBLISHED, label: '已发布' },
  { value: NOTICE_STATUS.OFFLINE, label: '已下线' },
  { value: NOTICE_STATUS.SCHEDULED, label: '待发布' },
] as const

export const NOTICE_STATUS_TAG_TYPE: Record<number, 'info' | 'success' | 'warning' | 'danger'> = {
  [NOTICE_STATUS.DRAFT]: 'info',
  [NOTICE_STATUS.PUBLISHED]: 'success',
  [NOTICE_STATUS.OFFLINE]: 'warning',
  [NOTICE_STATUS.SCHEDULED]: 'danger',
}

/** 公告可见性 */
export const NOTICE_VISIBILITY = {
  GUEST: 1,
  USER: 2,
  ADMIN: 3,
} as const

export const NOTICE_VISIBILITY_OPTIONS = [
  { value: NOTICE_VISIBILITY.GUEST, label: '所有人可见（游客及以上）' },
  { value: NOTICE_VISIBILITY.USER, label: '仅登录用户可见' },
  { value: NOTICE_VISIBILITY.ADMIN, label: '仅管理员可见' },
] as const
