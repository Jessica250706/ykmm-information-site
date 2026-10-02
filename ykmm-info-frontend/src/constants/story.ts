/* -------- 剧情分类类型 -------- */
export const STORY_CATEGORY_TYPE = {
  MAIN: 1,
  RAINBOW_CITY: 2,
  SPECIAL: 3,
  ACTIVITY: 4,
  DRAMA: 5,
} as const

export type StoryCategoryTypeValue = (typeof STORY_CATEGORY_TYPE)[keyof typeof STORY_CATEGORY_TYPE]

export const STORY_CATEGORY_TYPE_LABEL: Record<StoryCategoryTypeValue, string> = {
  [STORY_CATEGORY_TYPE.MAIN]: '主线',
  [STORY_CATEGORY_TYPE.RAINBOW_CITY]: '彩虹城',
  [STORY_CATEGORY_TYPE.SPECIAL]: '特别篇',
  [STORY_CATEGORY_TYPE.ACTIVITY]: '活动篇',
  [STORY_CATEGORY_TYPE.DRAMA]: '戏剧篇',
}

export const STORY_CATEGORY_TYPE_OPTIONS = [
  { value: STORY_CATEGORY_TYPE.MAIN, label: STORY_CATEGORY_TYPE_LABEL[STORY_CATEGORY_TYPE.MAIN] },
  {
    value: STORY_CATEGORY_TYPE.RAINBOW_CITY,
    label: STORY_CATEGORY_TYPE_LABEL[STORY_CATEGORY_TYPE.RAINBOW_CITY],
  },
  {
    value: STORY_CATEGORY_TYPE.SPECIAL,
    label: STORY_CATEGORY_TYPE_LABEL[STORY_CATEGORY_TYPE.SPECIAL],
  },
  {
    value: STORY_CATEGORY_TYPE.ACTIVITY,
    label: STORY_CATEGORY_TYPE_LABEL[STORY_CATEGORY_TYPE.ACTIVITY],
  },
  {
    value: STORY_CATEGORY_TYPE.DRAMA,
    label: STORY_CATEGORY_TYPE_LABEL[STORY_CATEGORY_TYPE.DRAMA],
  },
] as const

/* -------- 剧情审核状态 -------- */
export const STORY_STATUS = {
  PUBLISHED: 1,
  PENDING: 2,
  REJECTED: 3,
} as const

export type StoryStatusValue = (typeof STORY_STATUS)[keyof typeof STORY_STATUS]

export const STORY_STATUS_LABEL: Record<StoryStatusValue, string> = {
  [STORY_STATUS.PUBLISHED]: '已发布',
  [STORY_STATUS.PENDING]: '待审核',
  [STORY_STATUS.REJECTED]: '已拒绝',
}

export const STORY_STATUS_OPTIONS = [
  { value: STORY_STATUS.PUBLISHED, label: STORY_STATUS_LABEL[STORY_STATUS.PUBLISHED] },
  { value: STORY_STATUS.PENDING, label: STORY_STATUS_LABEL[STORY_STATUS.PENDING] },
  { value: STORY_STATUS.REJECTED, label: STORY_STATUS_LABEL[STORY_STATUS.REJECTED] },
] as const

/** el-tag 的 type 映射 */
export const STORY_STATUS_TAG_TYPE: Record<StoryStatusValue, 'success' | 'warning' | 'danger'> = {
  [STORY_STATUS.PUBLISHED]: 'success',
  [STORY_STATUS.PENDING]: 'warning',
  [STORY_STATUS.REJECTED]: 'danger',
}

/* -------- 审核操作 -------- */
export const STORY_AUDIT_ACTION = {
  APPROVE: 1,
  REJECT: 3,
} as const

export type StoryAuditActionValue = (typeof STORY_AUDIT_ACTION)[keyof typeof STORY_AUDIT_ACTION]
