import type { PageRequest } from './common'

export interface CardCategoryPageQueryDTO extends PageRequest {
  /**
   * 类别名模糊搜索
   */
  keyword?: string
  [property: string]: any
}

/**
 * 卡面类别返回
 */
export interface CardCategoryVO {
  /**
   * 关联的卡面数量（仅分页查询返回，其它接口为 null）
   */
  cardCount?: number
  /**
   * 主键
   */
  id?: number
  /**
   * 类别名
   */
  name?: string
  /**
   * 排序
   */
  sort?: number
  [property: string]: any
}

/**
 * 新增参数
 */
export interface CardCategoryDTO {
  /**
   * 类别名
   * <p>新增必填，长度 1~64；编辑可为 null（不改）
   */
  name: string
  /**
   * 排序：值越小越靠前；不传默认 0
   */
  sort?: number
  [property: string]: any
}
