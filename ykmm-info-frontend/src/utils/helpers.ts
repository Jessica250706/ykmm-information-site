import { STORY_CATEGORY_TYPE, STORY_STATUS } from '@/constants/story'

export type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

export function categoryTypeTag(type?: number): TagType {
  switch (type) {
    case STORY_CATEGORY_TYPE.MAIN:
      return 'danger'
    case STORY_CATEGORY_TYPE.RAINBOW_CITY:
      return 'warning'
    case STORY_CATEGORY_TYPE.SPECIAL:
      return 'success'
    case STORY_CATEGORY_TYPE.ACTIVITY:
      return 'primary'
    case STORY_CATEGORY_TYPE.DRAMA:
      return 'info'
    default:
      return 'info'
  }
}

export function storyStatusTag(status?: number): TagType {
  switch (status) {
    case STORY_STATUS.PUBLISHED:
      return 'success'
    case STORY_STATUS.PENDING:
      return 'warning'
    case STORY_STATUS.REJECTED:
      return 'danger'
    default:
      return 'info'
  }
}

/**
 * 按颜色变量生成 el-tag 的内联样式：
 * <el-tag :style="categoryTagStyle(color)" effect="plain">
 * → borderColor: var(--color-xxx); color: var(--color-xxx)
 */
export function categoryTagStyle(color: string) {
  return {
    borderColor: `var(--color-${color})`,
    color: `var(--color-${color})`,
  }
}
