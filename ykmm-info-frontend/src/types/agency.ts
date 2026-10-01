/**
 * AgencyVO
 */
export interface AgencyVO {
  /**
   * 主键
   */
  id?: number
  /**
   * 公司名
   */
  name?: string
  [property: string]: any
}

/**
 * 新增参数
 *
 * AgencyDTO
 */
export interface AgencyDTO {
  /**
   * 公司名
   */
  name?: string
  [property: string]: any
}
