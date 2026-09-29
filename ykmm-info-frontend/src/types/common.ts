/**
 * 数据
 *
 * PageResult
 */
export interface PageResult<T> {
  /**
   * 当前页数据
   */
  records?: T[]
  /**
   * 总记录数
   */
  total?: number
  [property: string]: any
}
