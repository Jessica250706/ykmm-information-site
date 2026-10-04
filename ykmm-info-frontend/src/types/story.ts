import type { PageRequest } from './common'
import type { DialogueLineVO } from './dialogueLine'

export interface StoryPageQueryDTO extends PageRequest {
  /**
   * 所属分类ID
   */
  categoryId?: number
  /**
   * 分类类型：1主线 2彩虹城 3特别篇 4活动篇 5戏剧篇
   */
  categoryType?: number
  /**
   * 关键字：标题模糊匹配
   */
  keyword?: string
  /**
   * 审核状态：1已发布 2待审核 3已拒绝
   */
  status?: number
  [property: string]: any
}

/**
 * 剧情返回
 *
 * StoryVO
 */
export interface StoryVO {
  /**
   * 所属分类节点
   */
  categoryId?: number
  /**
   * 所属分类名
   */
  categoryName?: string
  /**
   * 分类类型
   */
  categoryType?: number
  /**
   * 分类类型标签
   */
  categoryTypeLabel?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 创建者用户ID
   */
  creatorId?: number
  /**
   * 描述
   */
  description?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 审核人用户ID
   */
  reviewerId?: number
  /**
   * 审核备注
   */
  reviewRemark?: string
  /**
   * 审核时间
   */
  reviewTime?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 审核状态
   */
  status?: number
  /**
   * 审核状态标签
   */
  statusLabel?: string
  /**
   * 话标题
   */
  title?: string
  /**
   * 更新时间
   */
  updatedAt?: string
  [property: string]: any
}

/**
 * 新增参数
 */
export interface StoryDTO {
  /**
   * 所属分类节点
   */
  categoryId?: number
  /**
   * 描述
   */
  description?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 话标题
   */
  title?: string
  [property: string]: any
}

/**
 * 审核参数
 *
 * StoryAuditDTO
 */
export interface StoryAuditDTO {
  /**
   * 审核备注
   */
  reviewRemark?: string
  /**
   * 审核结果：1通过 3拒绝
   */
  status?: number
  [property: string]: any
}

/**
 * 数据
 *
 * StoryDetailVO
 */
export interface StoryDetailVO {
  /**
   * 所属分类ID
   */
  categoryId?: number
  /**
   * 分类名
   */
  categoryName?: string
  /**
   * 分类类型
   */
  categoryType?: number
  /**
   * 分类类型标签
   */
  categoryTypeLabel?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 描述
   */
  description?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 排序
   */
  sort?: number
  /**
   * 话标题
   */
  title?: string
  /**
   * 更新时间
   */
  updatedAt?: string
  /**
   * 对话版本列表
   */
  versions?: DialogueVersionVO[]
  [property: string]: any
}

/**
 * 对话版本
 *
 * DialogueVersionVO
 */
export interface DialogueVersionVO {
  /**
   * 1文字 2图片
   */
  format?: number
  formatLabel?: string
  id?: number
  images?: DialogueImageVO[]
  /**
   * 1中文 2日文
   */
  language?: number
  languageLabel?: string
  lines?: DialogueLineVO[]
  /**
   * 1全部 2节选
   */
  scope?: number
  scopeLabel?: string
  [property: string]: any
}

/**
 * 对话图片
 *
 * DialogueImageVO
 */
export interface DialogueImageVO {
  id?: number
  sort?: number
  url?: string
  [property: string]: any
}

/**
 * 对话片段
 *
 * DialogueSegmentVO
 */
export interface DialogueSegmentVO {
  content?: string
  id?: number
  /**
   * 1文本 2表情包
   */
  segmentType?: number
  sort?: number
  stickerEmoji?: string
  stickerId?: number
  stickerUrl?: string
  [property: string]: any
}
