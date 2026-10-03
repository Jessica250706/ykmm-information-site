/**
 * 剧情分类类型
 *
 * StoryCategoryTypeVO
 */
export interface StoryCategoryTypeVO {
  /**
   * 标签颜色
   */
  color?: string
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
   * 名称
   */
  name?: string
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
 * 编辑参数
 */
export interface StoryCategoryTypeDTO {
  /**
   * 标签颜色
   */
  color?: string
  /**
   * 描述
   */
  description?: string
  /**
   * 名称
   */
  name?: string
  /**
   * 排序
   */
  sort?: number
  [property: string]: any
}
