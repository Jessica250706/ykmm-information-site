import type { PageRequest } from './common'

export interface CardSeriesPageQueryDTO extends PageRequest {
  /**
   * 关键字：系列名模糊匹配
   */
  keyword?: string
  [property: string]: any
}

/**
 * 卡面系列返回
 *
 * CardSeriesVO
 */
export interface CardSeriesVO {
  /**
   * 关联卡面数量
   */
  cardCount?: number
  /**
   * 封面图地址
   */
  coverImage?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 系列描述
   */
  description?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 系列名
   */
  name?: string
  /**
   * 更新时间
   */
  updatedAt?: string
  [property: string]: any
}

/**
 * 新增参数
 */
export interface CardSeriesDTO {
  /**
   * 封面图地址
   */
  coverImage?: string
  /**
   * 系列描述
   */
  description?: string
  /**
   * 系列名
   */
  name?: string
  [property: string]: any
}
