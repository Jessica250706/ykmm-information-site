/** 通用分页请求参数 */
export interface PageRequest {
  /** 页码，从 1 开始 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

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
