/**
 * 卡面 Rabitter 返回
 */
export interface CardRabitterVO {
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
   * 标题
   */
  title?: string
  [property: string]: any
}

/**
 * 新增参数
 */
export interface CardRabitterDTO {
  /**
   * 卡面ID
   */
  cardId?: number
  /**
   * 第几话
   */
  episodeNo?: number
  /**
   * 话标题
   */
  title?: string
  [property: string]: any
}
