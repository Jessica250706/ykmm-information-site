import type { PageRequest } from './common'
import type { StoryCategoryVO } from './storyCategory'

export interface RolePageQueryDTO extends PageRequest {
  /**
   * 关键字：角色名
   */
  keyword?: string
  /**
   * 对应人物ID
   */
  personId?: number
  /**
   * 剧情分类根节点ID
   */
  storyCategoryId?: number
  [property: string]: any
}

/**
 * 角色返回
 *
 * RoleVO
 */
export interface RoleVO {
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 角色简介
   */
  intro?: string
  /**
   * 角色名称
   */
  name?: string
  /**
   * 对应人物ID，可为空
   */
  personId?: number
  /**
   * 对应人物中文名，可为空
   */
  personNameCn?: string
  /**
   * 所属剧情分类根节点
   */
  storyCategories?: StoryCategoryVO[]
  /**
   * 更新时间
   */
  updatedAt?: string
  [property: string]: any
}

/**
 * 新增参数
 */
export interface RoleDTO {
  /**
   * 角色简介
   */
  intro?: string
  /**
   * 角色名称
   */
  name?: string
  /**
   * 对应人物ID，可为空
   */
  personId?: number
  /**
   * 所属剧情分类根节点ID列表
   */
  storyCategoryIds?: number[]
  [property: string]: any
}
