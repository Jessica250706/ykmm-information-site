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

/* -------- 审核操作 -------- */
export const STORY_AUDIT_ACTION = {
  APPROVE: 1,
  REJECT: 3,
} as const

export type StoryAuditActionValue = (typeof STORY_AUDIT_ACTION)[keyof typeof STORY_AUDIT_ACTION]
