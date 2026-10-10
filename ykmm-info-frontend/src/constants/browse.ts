/**
 * 浏览中心 kind
 * story   - 查看某一话剧情详情
 * category - 查看某一分类（展示子分类 / 剧情列表）
 */
export const BROWSE_KIND = {
  STORY: 'story',
  CATEGORY: 'category',
} as const

export type BrowseKind = (typeof BROWSE_KIND)[keyof typeof BROWSE_KIND]

export const BROWSE_KIND_LABEL: Record<BrowseKind, string> = {
  [BROWSE_KIND.STORY]: '剧情',
  [BROWSE_KIND.CATEGORY]: '分类',
}

export const BROWSE_KIND_OPTIONS = [
  { value: BROWSE_KIND.STORY, label: BROWSE_KIND_LABEL[BROWSE_KIND.STORY] },
  { value: BROWSE_KIND.CATEGORY, label: BROWSE_KIND_LABEL[BROWSE_KIND.CATEGORY] },
] as const

/** 是否剧情模式 */
export function isStoryKind(kind?: string | null): boolean {
  return kind === BROWSE_KIND.STORY
}

/** 是否分类模式 */
export function isCategoryKind(kind?: string | null): boolean {
  return kind === BROWSE_KIND.CATEGORY
}

/** 安全取文案 */
export function browseKindLabel(kind?: string | null): string {
  if (isStoryKind(kind)) return BROWSE_KIND_LABEL[BROWSE_KIND.STORY]
  if (isCategoryKind(kind)) return BROWSE_KIND_LABEL[BROWSE_KIND.CATEGORY]
  return '未知'
}
