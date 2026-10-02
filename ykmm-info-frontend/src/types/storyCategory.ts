import type { PageRequest } from './common'

export interface StoryCategoryQueryDTO {
  /**
   * 分类类型，可选
   */
  categoryType?: number
  [property: string]: any
}

export interface StoryCategoryPageQueryDTO extends PageRequest {
  /**
   * 分类类型，可选
   */
  categoryType?: number
  [property: string]: any
}

/**
 * 剧情分类返回
 *
 * StoryCategoryVO
 */
export interface StoryCategoryVO {
  /**
   * 1主线 2彩虹城 3特别篇 4活动篇 5戏剧篇
   */
  categoryType?: number
  /**
   * 分类类型标签
   */
  categoryTypeLabel?: string
  /**
   * 子分类
   */
  children?: StoryCategoryVO[]
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 分类名
   */
  name?: string
  /**
   * 父分类ID
   */
  parentId?: number
  /**
   * 排序
   */
  sort?: number
  /**
   * 更新时间
   */
  updatedAt?: string
  [property: string]: any
}

/**
 * 新增参数
 */
export interface StoryCategoryDTO {
  /**
   * 1主线 2彩虹城 3特别篇 4活动篇 5戏剧篇
   */
  categoryType?: number
  /**
   * 分类名
   */
  name?: string
  /**
   * 父分类ID，0 表示根节点
   */
  parentId?: number
  /**
   * 排序
   */
  sort?: number
  [property: string]: any
}
