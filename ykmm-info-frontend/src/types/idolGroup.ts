/**
 * IdolGroupVO
 */
export interface IdolGroupVO {
  /**
   * 所属经纪公司
   */
  agencyId?: number
  /**
   * 所属经纪公司名
   */
  agencyName?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 团体名
   */
  name?: string
  [property: string]: any
}

/**
 * 新增参数
 *
 * IdolGroupDTO
 */
export interface IdolGroupDTO {
  /**
   * 所属经纪公司ID
   */
  agencyId?: number
  /**
   * 团体名
   */
  name?: string
  [property: string]: any
}

/**
 * PersonIdolGroupVO
 */
export interface PersonIdolGroupVO {
  /**
   * 主键
   */
  id?: number
  /**
   * 团体名
   */
  name?: string
  [property: string]: any
}
