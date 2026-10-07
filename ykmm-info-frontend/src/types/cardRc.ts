/**
 * 卡面 RC 返回
 */
export interface CardRcVO {
  /**
   * 所属卡面ID
   */
  cardId?: number
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 第几话
   */
  episodeNo?: number
  /**
   * 主键
   */
  id?: number
  /**
   * 发起人角色ID
   */
  roleId?: number
  /**
   * 发起人角色名
   */
  roleName?: string
  /**
   * 标题
   */
  title?: string
  [property: string]: any
}

/**
 * CardRcDTO
 */
export interface CardRcDTO {
  /**
   * 所属卡面ID
   */
  cardId?: number
  /**
   * 第几话
   */
  episodeNo?: number
  /**
   * RC发起人角色ID，该角色对话默认右侧（side=2）
   */
  roleId?: number
  /**
   * 标题
   */
  title?: string
  [property: string]: any
}
