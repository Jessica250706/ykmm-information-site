import type { PageRequest } from './common'

export interface NoticePageQueryDTO extends PageRequest {
  /**
   * 分类ID
   */
  categoryId?: number
  /**
   * 是否置顶（仅管理端生效）
   */
  isTop?: number
  /**
   * 关键词：标题模糊匹配
   */
  keyword?: string
  /**
   * 最低可见级别（仅管理端生效）
   */
  minVisibility?: number
  /**
   * 状态（仅管理端生效）：0-草稿 1-已发布 2-已下线
   */
  status?: number
  [property: string]: any
}

/**
 * 公告 VO
 */
export interface NoticeVO {
  /**
   * 分类颜色
   */
  categoryColor?: string
  categoryId?: number
  /**
   * 分类名称
   */
  categoryName?: string
  content?: string
  coverImage?: string
  createdAt?: string
  creatorId?: number
  /**
   * 创建者昵称（优先昵称，回退邮箱）
   */
  creatorName?: string
  expireAt?: string
  id?: number
  isTop?: number
  minVisibility?: number
  /**
   * 可见性标签
   */
  minVisibilityLabel?: string
  publishAt?: string
  status?: number
  /**
   * 状态标签：草稿/已发布/已下线
   */
  statusLabel?: string
  summary?: string
  title?: string
  updatedAt?: string
  viewCount?: number
  [property: string]: any
}

/**
 * 公告分类 VO
 */
export interface NoticeCategoryVO {
  color?: string
  description?: string
  id?: number
  name?: string
  sort?: number
  [property: string]: any
}

/**
 * NoticeDTO
 */
export interface NoticeDTO {
  categoryId: number
  content: string
  coverImage?: string
  /**
   * 过期时间；为空时永不过期
   */
  expireAt?: string
  /**
   * 是否置顶：0-否 1-是
   */
  isTop?: number
  minVisibility: number
  /**
   * 发布时间；为空时按创建时间算
   */
  publishAt?: string
  summary?: string
  title: string
  [property: string]: any
}
